package com.black.http.service;


import com.black.http.Request;
import com.black.http.Response;

import java.util.LinkedList;

public class BaseFilterChain implements FilterChain{

    private final LinkedList<Filter> requestFilters = new LinkedList<>();

    private final LinkedList<Filter> responseFilters = new LinkedList<>();

    private Request request;

    private Response response;

    void addFilters(LinkedList<Filter> filters){
        requestFilters.addAll(filters);
    }

    void addFilter(Filter filter){
        requestFilters.add(filter);
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    @Override
    public void fireRequest() throws Throwable {
        Filter filter = requestFilters.poll();
        if (filter != null){
            filter.doFilterRequest(this, request);
            responseFilters.addFirst(filter);
        }
    }

    @Override
    public void fireResponse() throws Throwable {
        Filter filter = responseFilters.poll();
        if (filter != null){
            filter.doFilterResponse(this, response);
        }
    }
}
