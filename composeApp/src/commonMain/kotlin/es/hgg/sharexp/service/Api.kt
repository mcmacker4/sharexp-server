package es.hgg.sharexp.service

import org.jetbrains.compose.resources.getString
import sharexp.composeapp.generated.resources.Res
import sharexp.composeapp.generated.resources.api_base_url


suspend fun getBaseUrl(): String = getString(Res.string.api_base_url)