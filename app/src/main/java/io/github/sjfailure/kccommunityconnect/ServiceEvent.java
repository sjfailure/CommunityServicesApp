package io.github.sjfailure.kccommunityconnect;

public class ServiceEvent {

    private final String id;
    private final String providerName;
    private final String serviceCategory;
    private final String serviceType;
    private final String startTime;

    public ServiceEvent(String id, String providerName, String serviceCategory, String serviceType, String startTime) {
        this.id = id;
        this.providerName = providerName;
        this.serviceCategory = serviceCategory;
        this.serviceType = serviceType;
        this.startTime = startTime;
    }

    public String getId() {
        return id;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }

    public String getServiceType() {
        return serviceType;
    }

    public String getStartTime() {
        return startTime;
    }
}
