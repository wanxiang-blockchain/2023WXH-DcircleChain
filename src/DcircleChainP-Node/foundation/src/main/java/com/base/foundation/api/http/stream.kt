package com.base.foundation.api.http

import android.util.Log
import com.anywithyou.stream.Client
import com.anywithyou.stream.Client.PeerClosedCallback
import com.anywithyou.stream.Duration
import com.anywithyou.stream.Option
import com.base.foundation.getUs
import com.base.foundation.nc.NcEvent
import com.base.foundation.utils.Tuple
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StreamDisconnected : NcEvent<String>()

class StreamConnected : NcEvent<String>()

class Stream(private val builder: HttpBuilder) : Http {
	val scope = CoroutineScope(Dispatchers.IO)

	override suspend fun send(): Tuple<String, Error?> {
		var client = allClients[this.builder.baseUrl()]
		if (client == null) {
			val paths = builder.baseUrl().split("://")
            require(paths.size == 2) {"baseUrl("+builder.baseUrl()+") not start with wss:// or ws://"}

			val uriS = paths[1].split(":")
			require(uriS.size == 2) {"can not parse baseUrl("+builder.baseUrl()+")'s host and port"}

			val host = uriS[0]
			val port = uriS[1].toInt()
			Log.d(::Stream::class.java.simpleName, "send host(${host}) port(${port})")
			client = if (paths[0]=="wss") {
				Client(Option.Host(host), Option.Port(port), Option.TLS(),
					Option.ConnectTimeout(Duration(5*Duration.Second)), Option.RequestTimeout(Duration(60*Duration.Second)))
			} else {
				Client(Option.Host(host), Option.Port(port),
					Option.ConnectTimeout(Duration(5*Duration.Second)), Option.RequestTimeout(Duration(60*Duration.Second)))
			}

//			client.setPushCallback(this.builder.pusher_)
			client.setPeerClosedCallback(PeerClosedCallback {
                scope.launch {
                    Log.d("AppState", "Stream onPeerClosed")
                    getUs().nc.postToMain(StreamDisconnected())
                }

                client.Recover(object : Client.RecoverHandler {
                    override fun onFailed(error: java.lang.Error?, isConnError: Boolean) {
                        Log.d("AppState", "Stream onPeerClosed client.Recover onFailed, continue Recover")
                        client.Recover(this)
                    }

                    override fun onSuccess() {
                        scope.launch {
                            Log.d("AppState", "Stream onPeerClosed client.Recover onSuccess postToMain StreamConnected")
                            getUs().nc.postToMain(StreamConnected())
                        }
                    }
                })
            })
			allClients[this.builder.baseUrl()] = client
		}

		val wait:Channel<Tuple<String, Error?>> = Channel(1)
		scope.launch {
			withContext(Dispatchers.Main) {
				client.Send(this@Stream.builder.content().toByteArray(), this@Stream.builder.headers(), object :
					Client.ResponseHandler {
					override fun onFailed(error: Error?, isConnError: Boolean) {
						Log.w(::Stream::class.java.simpleName, "error ${error?.localizedMessage} isConnError $isConnError")
						scope.launch {
							wait.send(Tuple("", error))
						}
					}

					override fun onSuccess(response: ByteArray?) {
						response?.apply {
							scope.launch {
								wait.send(Tuple(String(response), null))
							}
							return
                        }
					}
				})
			}
		}
		return wait.receive()
	}

	companion object {
		var allClients:MutableMap<String, Client> = mutableMapOf()
	}
}

class StreamBuilder(var baseUrl: String): HttpBuilder(baseUrl) {
	override fun build(): Http {
		return Stream(this)
	}
}
fun StreamBuilderCreator():(baseUrl:String) -> HttpBuilder {
	return {baseUrl: String ->  StreamBuilder(baseUrl)}
}