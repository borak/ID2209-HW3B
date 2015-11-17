package se.kth.id2209.hw1.smartmuseum;

import java.util.ArrayList;

import se.kth.id2209.hw1.exhibition.Artifact;
import se.kth.id2209.hw1.profiler.ProfilerAgent;

class Tour {
	private TourGuideAgent tourGuide;
	private ArrayList<Artifact> artifacts;
	private ArrayList<ProfilerAgent> profilers;

	public Tour(TourGuideAgent tourGuide) {
		this.setTourGuide(tourGuide);
	}

	ArrayList<Artifact> getArtifacts() {
		return artifacts;
	}
	void addArtifact(Artifact artifact) {
		artifacts.add(artifact);
	}
	void setArtifacts(ArrayList<Artifact> artifacts) {
		this.artifacts = artifacts;
	}
	ArrayList<ProfilerAgent> getProfilers() {
		return profilers;
	}
	void setProfilers(ArrayList<ProfilerAgent> profilers) {
		this.profilers = profilers;
	}

	public TourGuideAgent getTourGuide() {
		return tourGuide;
	}

	public void setTourGuide(TourGuideAgent tourGuide) {
		this.tourGuide = tourGuide;
	}

	public void addProfiler(ProfilerAgent profiler) {
		profilers.add(profiler);		
	}

	public void quitTour(ProfilerAgent profiler) {
		if(profilers.contains(profiler))
			profilers.remove(profiler);
	}


}
