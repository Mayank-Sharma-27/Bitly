package model;

import java.util.Optional;

public interface UrlMappingDAO {

    Optional<UrlMapping> getRedirectUrl(String shortUrlId);

    void createUrlMapping(String longUrl);

}
