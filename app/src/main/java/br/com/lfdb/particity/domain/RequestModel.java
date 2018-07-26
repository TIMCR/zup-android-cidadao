package br.com.lfdb.particity.domain;

public class RequestModel {
  private double latitude;
  private double longitude;
  private long raio;

  public RequestModel(double latitude, double longitude, long raio){
    this.latitude = latitude;
    this.longitude = longitude;
    this.raio = raio;
  }

  public double getLatitude(){
    return latitude;
  }

  public double getLongitude(){
    return longitude;
  }

  public long getRaio(){
    return raio;
  }
}