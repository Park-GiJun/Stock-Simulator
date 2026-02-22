package com.stocksimulator.eventservice.application.port.out

import com.stocksimulator.eventservice.domain.model.NewsArticleModel

interface NewsEventPort {
    fun publishNewsPublished(news: NewsArticleModel)
}
