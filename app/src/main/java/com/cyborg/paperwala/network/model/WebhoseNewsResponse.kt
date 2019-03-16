/*
 * Copyright 2019 randhirgupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cyborg.paperwala.network.model

data class WebhoseNewsResponse(
        val moreResultsAvailable: Int,
        val next: String,
        val posts: List<Post>,
        val requestsLeft: Int,
        val totalResults: Int,
        val warnings: Any
)

data class Post(
        val author: String,
        val crawled: String,
        val entities: Entities,
        val external_links: List<Any>,
        val highlightText: String,
        val highlightTitle: String,
        val language: String,
        val ord_in_thread: Int,
        val published: String,
        val rating: Any,
        val text: String,
        val thread: Thread,
        val title: String,
        val url: String,
        val uuid: String
)

data class Entities(
        val locations: List<Any>,
        val organizations: List<Any>,
        val persons: List<Any>
)

data class Thread(
        val country: String,
        val domain_rank: Int,
        val main_image: String,
        val participants_count: Int,
        val performance_score: Int,
        val published: String,
        val replies_count: Int,
        val section_title: String,
        val site: String,
        val site_categories: List<String>,
        val site_full: String,
        val site_section: String,
        val site_type: String,
        val social: Social,
        val spam_score: Int,
        val title: String,
        val title_full: String,
        val url: String,
        val uuid: String
)

data class Social(
        val facebook: Facebook,
        val gplus: Gplus,
        val linkedin: Linkedin,
        val pinterest: Pinterest,
        val stumbledupon: Stumbledupon,
        val vk: Vk
)

data class Vk(
        val shares: Int
)

data class Stumbledupon(
        val shares: Int
)

data class Facebook(
        val comments: Int,
        val likes: Int,
        val shares: Int
)

data class Pinterest(
        val shares: Int
)

data class Linkedin(
        val shares: Int
)

data class Gplus(
        val shares: Int
)