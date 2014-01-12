package com.riddimon.pickpix.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImageSearchResult extends ServiceResult {
	@JsonProperty("results")
	List<ImageResult> results;

	@JsonProperty("cursor")
	ResultCursor cursor;
}
