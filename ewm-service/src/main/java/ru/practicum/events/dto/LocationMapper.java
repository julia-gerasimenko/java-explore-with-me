package ru.practicum.events.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.events.model.Location;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocationMapper {

    public static Location mapToLocation(LocationDto locationDto) {
        Location location = new Location();
        location.setLat(locationDto.getLat());
        location.setLon(locationDto.getLon());
        return location;
    }

    public static LocationDto mapToLocationDto(Location location) {
        LocationDto locationDto = new LocationDto();
        locationDto.setLat(location.getLat());
        locationDto.setLon(location.getLon());
        return locationDto;

    }
}
