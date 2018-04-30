package me.shouheng.omnilist.model.tools;

import java.util.List;

import me.shouheng.omnilist.model.Location;

/**
 * Created by wang shouheng on 2018/1/19.*/
public class Stats {

    private int totalCategories;

    private int totalAssignments;
    private int archivedAssignments;
    private int trashedAssignments;
    private List<Integer> assignmentsStats;

    private int totalSubAssignments;
    private int archivedSubAssignments;
    private int trashedSubAssignments;

    private List<Location> locations;
    private int locCnt;
    private int totalLocations;

    private int totalAttachments;
    private int images;
    private int videos;
    private int audioRecordings;
    private int sketches;
    private int files;

    private int totalAlarms;

    public int getTotalCategories() {
        return totalCategories;
    }

    public void setTotalCategories(int totalCategories) {
        this.totalCategories = totalCategories;
    }

    public int getTotalAssignments() {
        return totalAssignments;
    }

    public void setTotalAssignments(int totalAssignments) {
        this.totalAssignments = totalAssignments;
    }

    public int getArchivedAssignments() {
        return archivedAssignments;
    }

    public void setArchivedAssignments(int archivedAssignments) {
        this.archivedAssignments = archivedAssignments;
    }

    public int getTrashedAssignments() {
        return trashedAssignments;
    }

    public void setTrashedAssignments(int trashedAssignments) {
        this.trashedAssignments = trashedAssignments;
    }

    public List<Integer> getAssignmentsStats() {
        return assignmentsStats;
    }

    public void setAssignmentsStats(List<Integer> assignmentsStats) {
        this.assignmentsStats = assignmentsStats;
    }

    public int getTotalSubAssignments() {
        return totalSubAssignments;
    }

    public void setTotalSubAssignments(int totalSubAssignments) {
        this.totalSubAssignments = totalSubAssignments;
    }

    public int getArchivedSubAssignments() {
        return archivedSubAssignments;
    }

    public void setArchivedSubAssignments(int archivedSubAssignments) {
        this.archivedSubAssignments = archivedSubAssignments;
    }

    public int getTrashedSubAssignments() {
        return trashedSubAssignments;
    }

    public void setTrashedSubAssignments(int trashedSubAssignments) {
        this.trashedSubAssignments = trashedSubAssignments;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public int getLocCnt() {
        return locCnt;
    }

    public void setLocCnt(int locCnt) {
        this.locCnt = locCnt;
    }

    public int getTotalLocations() {
        return totalLocations;
    }

    public void setTotalLocations(int totalLocations) {
        this.totalLocations = totalLocations;
    }

    public int getTotalAttachments() {
        return totalAttachments;
    }

    public void setTotalAttachments(int totalAttachments) {
        this.totalAttachments = totalAttachments;
    }

    public int getImages() {
        return images;
    }

    public void setImages(int images) {
        this.images = images;
    }

    public int getVideos() {
        return videos;
    }

    public void setVideos(int videos) {
        this.videos = videos;
    }

    public int getAudioRecordings() {
        return audioRecordings;
    }

    public void setAudioRecordings(int audioRecordings) {
        this.audioRecordings = audioRecordings;
    }

    public int getSketches() {
        return sketches;
    }

    public void setSketches(int sketches) {
        this.sketches = sketches;
    }

    public int getFiles() {
        return files;
    }

    public void setFiles(int files) {
        this.files = files;
    }

    public int getTotalAlarms() {
        return totalAlarms;
    }

    public void setTotalAlarms(int totalAlarms) {
        this.totalAlarms = totalAlarms;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "totalCategories=" + totalCategories +
                ", totalAssignments=" + totalAssignments +
                ", archivedAssignments=" + archivedAssignments +
                ", trashedAssignments=" + trashedAssignments +
                ", assignmentsStats=" + assignmentsStats +
                ", totalSubAssignments=" + totalSubAssignments +
                ", archivedSubAssignments=" + archivedSubAssignments +
                ", trashedSubAssignments=" + trashedSubAssignments +
                ", locations=" + locations +
                ", locCnt=" + locCnt +
                ", totalLocations=" + totalLocations +
                ", totalAttachments=" + totalAttachments +
                ", images=" + images +
                ", videos=" + videos +
                ", audioRecordings=" + audioRecordings +
                ", sketches=" + sketches +
                ", files=" + files +
                ", totalAlarms=" + totalAlarms +
                '}';
    }
}
