package com.example.compass;

public class Place {

    double latitude;
    double longitude;

    public Place(){
    }

    public Place(double lat, double lng){
        latitude = lat;
        longitude = lng;
    }

    public double meterDistanceBetweenPoints(double lat2, double lng2) {
        double latDistance = Math.toRadians(lat2 - latitude);
        double lonDistance = Math.toRadians(lng2 - longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371 * c * 1000;
    }

    public float rotationAngle(double lat2, double lng2) {
        double lat1 = Math.toRadians(latitude);
        double lng1 = Math.toRadians(longitude);
        lat2 = Math.toRadians(lat2);
        lng2 = Math.toRadians(lng2);

        double dLon = (lng2 - lng1);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);
        brng = Math.toDegrees(brng);
        if(brng<0) brng = -(180 + brng);
        else brng = 180 - brng;

        return (float)brng;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
