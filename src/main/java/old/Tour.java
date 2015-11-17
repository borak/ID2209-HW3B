package old;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import se.kth.id2209.hw1.exhibition.Artifact;
import se.kth.id2209.hw1.profiler.ProfilerAgent;
import se.kth.id2209.hw1.profiler.UserProfile;
import se.kth.id2209.hw1.smartmuseum.TourGuideAgent;

class Tour {
	private TourGuideAgent tourGuide;
	private Map<ProfilerAgent, Artifact> profilers = new HashMap<>();

	public Tour(TourGuideAgent tourGuide) {
		this.setTourGuide(tourGuide);
	}

	ArrayList<Artifact> getArtifacts() {
		return artifacts;
	}
	
	void addArtifact(Artifact artifact) {
		profilers.add(artifact);
	}
	
	void setArtifacts(ArrayList<Artifact> artifacts) {
		this.artifacts = artifacts;
	}

	public TourGuideAgent getTourGuide() {
		return tourGuide;
	}

	public void setTourGuide(TourGuideAgent tourGuide) {
		this.tourGuide = tourGuide;
	}

	public void addProfiler(ProfilerAgent profiler, UserProfile userProfile) {
		profilers.put(profiler, userProfile);		
	}

	public void quitTour(ProfilerAgent profiler) {
		if(profilers.containsKey(profiler))
			profilers.remove(profiler);
	}

	public UserProfile getUserProfile(ProfilerAgent profiler) throws NullPointerException {		
		return profilers.get(profiler);
	}

	public Artifact getArtifact(ProfilerAgent profiler) {
		// TODO Auto-generated method stub
		return null;
	}


}
