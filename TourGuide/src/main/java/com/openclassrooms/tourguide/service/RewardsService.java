package com.openclassrooms.tourguide.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

@Service
public class RewardsService {
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private final GpsUtil gpsUtil;
	private final RewardCentral rewardsCentral;

	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}

	public void calculateRewards(User user) {
		System.out.println("Calculating rewards...");
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();

//        //association attractions/rewards > actual=52
//        //tester avec set
//        Map<String, UserReward> userRewardMap = user.getUserRewards()
//                .stream()
//                .collect(Collectors.toMap(
//                        userReward -> userReward.attraction.attractionName,
//                        userReward -> userReward
//                ));

		//rewarded attractions
		//thread safe set(1)
		Set<String> rewarded;
		synchronized (user.getUserRewards()) {
			rewarded = user.getUserRewards()
					.stream()
					.map(userReward -> userReward.attraction.attractionName)
					.collect(Collectors.toSet());
		}

		for(VisitedLocation visitedLocation : userLocations) {
			for(Attraction attraction : attractions) {
				//rewarded ?
	//				if(user.getUserRewards().stream().noneMatch(r -> r.attraction.attractionName.equals(attraction.attractionName)))
				if(rewarded.contains(attraction.attractionName)) {
					continue;}
					if(nearAttraction(visitedLocation, attraction)) {
						UserReward reward = new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user));

                        //thread safe set(2)
                        synchronized (user.getUserRewards()) {
                            if (user.getUserRewards().stream()
                                    .noneMatch(userReward -> userReward.attraction.attractionName.equals(attraction.attractionName))) {
                                user.addUserReward(reward);
                                rewarded.add(attraction.attractionName);

                        }
                    }
                }
            }
		}
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}

	private int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public double getDistance(Location loc1, Location loc2) {
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                               + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
	}

}
