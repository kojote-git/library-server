package com.jkojote.libraryserver.application.controllers.utils;

import com.jkojote.library.domain.shared.domain.DomainEntity;

import java.util.List;

public interface EntityUrlParamsFilter<T extends DomainEntity> {

    List<T> findAll(String url);

    List<T> findAllQueryString(String queryString);
}
