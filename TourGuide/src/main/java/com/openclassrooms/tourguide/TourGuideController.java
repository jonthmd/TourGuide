package com.openclassrooms.tourguide;

import java.util.List;

import com.openclassrooms.tourguide.dto.AttractionDTO;
import com.openclassrooms.tourguide.service.RewardsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import gpsUtil.location.VisitedLocation;

import com.openclassrooms.tourguide.service.TourGuideService;
import com.openclassrooms.tourguide.user.User;
import com.openclassrooms.tourguide.user.UserReward;

import tripPricer.Provider;

@Slf4j
@RestController
@Tag(name = "TourGuide", description = "TourGuide Controller.")
public class TourGuideController {

//	@Autowired
	private final TourGuideService tourGuideService;
    private final RewardsService rewardsService;

    public TourGuideController(TourGuideService tourGuideService, RewardsService rewardsService) {
        this.tourGuideService = tourGuideService;
        this.rewardsService = rewardsService;
    }

    @GetMapping("/")
    @Operation(summary = "Index", description = "Returns greetings text.")
    public String index() {
        return "Greetings from TourGuide!";
    }
    
    @GetMapping("/getLocation")
    @Operation(summary = "Get user location", description = "Returns the location user.")
    public VisitedLocation getLocation(@RequestParam String userName) {
    	return tourGuideService.getUserLocation(getUser(userName));
    }
    
    //  TODO: Change this method to no longer return a List of Attractions.
 	//  Instead: Get the closest five tourist attractions to the user - no matter how far away they are.
 	//  Return a new JSON object that contains:
    	// Name of Tourist attraction, 
        // Tourist attractions lat/long, 
        // The user's location lat/long, 
        // The distance in miles between the user's location and each of the attractions.
        // The reward points for visiting each Attraction.
        //    Note: Attraction reward points can be gathered from RewardsCentral
    @GetMapping("/getNearbyAttractions")
    @Operation(summary = "Get nearby attractions", description = "Returns the closest five tourist attractions to the user.")
    public List<AttractionDTO> getNearbyAttractions(@RequestParam String userName) {
    	VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
    	return tourGuideService.getNearByAttractions(visitedLocation)
                .stream()
                .map(attraction -> new AttractionDTO(
                        attraction.attractionName, attraction.latitude, attraction.longitude, visitedLocation.location.latitude, visitedLocation.location.longitude, rewardsService.getDistance(visitedLocation.location, attraction), rewardsService.getRewardPoints(attraction, getUser(userName))))
                .toList();
    }
    
    @GetMapping("/getRewards")
    @Operation(summary = "Get rewards", description = "Returns the user rewards.")
    public List<UserReward> getRewards(@RequestParam String userName) {
    	return tourGuideService.getUserRewards(getUser(userName));
    }
       
    @GetMapping("/getTripDeals")
    @Operation(summary = "Get trip deals", description = "Returns trip deals for the user.")
    public List<Provider> getTripDeals(@RequestParam String userName) {
    	return tourGuideService.getTripDeals(getUser(userName));
    }
    
    private User getUser(String userName) {
    	return tourGuideService.getUser(userName);
    }
}