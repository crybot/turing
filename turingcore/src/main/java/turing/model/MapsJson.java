package turing.model;

import org.json.JSONObject;

/**
 * Empty interface stating that there is a mapping between
 * the object implementing this interface and its json representation.
 */
public interface MapsJson {
    JSONObject toJson();
}
