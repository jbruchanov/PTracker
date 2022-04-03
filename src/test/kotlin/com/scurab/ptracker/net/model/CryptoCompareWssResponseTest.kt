package com.scurab.ptracker.net.model

import com.scurab.ptracker.app.serialisation.JsonBridge
import org.junit.jupiter.api.Test

internal class CryptoCompareWssResponseTest {
    @Test
    fun test() {
        val json = listOf(
            """{"TYPE":"20","MESSAGE":"STREAMERWELCOME","SERVER_UPTIME_SECONDS":706460,"SERVER_NAME":"30","SERVER_TIME_MS":1648585449387,"CLIENT_ID":2281390,"DATA_FORMAT":"JSON","SOCKET_ID":"AibX3y+aNEi85/v4Fli8","SOCKETS_ACTIVE":3,"SOCKETS_REMAINING":7,"RATELIMIT_MAX_SECOND":30,"RATELIMIT_MAX_MINUTE":60,"RATELIMIT_MAX_HOUR":1200,"RATELIMIT_MAX_DAY":10000,"RATELIMIT_MAX_MONTH":20000,"RATELIMIT_REMAINING_SECOND":29,"RATELIMIT_REMAINING_MINUTE":59,"RATELIMIT_REMAINING_HOUR":1199,"RATELIMIT_REMAINING_DAY":9993,"RATELIMIT_REMAINING_MONTH":19993}""",
            """{"TYPE":"2","MARKET":"Coinbase","FROMSYMBOL":"BTC","TOSYMBOL":"GBP","FLAGS":2,"PRICE":36406.1,"LASTUPDATE":1648585387,"LASTVOLUME":0.00000691,"LASTVOLUMETO":0.251566151,"LASTTRADEID":"29522097","VOLUMEDAY":425.25395693,"VOLUMEDAYTO":15442345.4602752,"VOLUME24HOUR":511.12132783,"VOLUME24HOURTO":18551479.241235,"OPENDAY":35953.28,"HIGHDAY":36626.24,"LOWDAY":35903.95,"OPEN24HOUR":36596.4,"HIGH24HOUR":36722.81,"LOW24HOUR":35729.03,"VOLUMEHOUR":1.36776446,"VOLUMEHOURTO":49782.6526630833,"OPENHOUR":36421.74,"HIGHHOUR":36423.12,"LOWHOUR":36367.24}""",
            """{"TYPE":"2","MARKET":"Coinbase","FROMSYMBOL":"ETH","TOSYMBOL":"USD","FLAGS":1,"PRICE":3415.01,"LASTUPDATE":1648580252,"LASTVOLUME":0.28706105,"LASTVOLUMETO":980.3163563605,"LASTTRADEID":"248382752","VOLUMEDAY":137131.22591151,"VOLUMEDAYTO":468638340.509405,"VOLUME24HOUR":167849.86450592,"VOLUME24HOURTO":572198304.26171,"OPENDAY":3334.47,"HIGHDAY":3483.87,"LOWDAY":3331.23,"OPEN24HOUR":3418.38,"HIGH24HOUR":3483.87,"LOW24HOUR":3316.92,"VOLUMEHOUR":0,"VOLUMEHOURTO":0,"OPENHOUR":3415.01,"HIGHHOUR":3415.01,"LOWHOUR":3415.01}""",
            """{ "TYPE":"3", "MESSAGE":"LOADCOMPLETE", "INFO":"All your valid subs have been loaded." }""",
            """{ "TYPE":"999", "MESSAGE":"HEARTBEAT", "TIMEMS":1648585465285 }""",
            """{ "TYPE":"2", "MARKET":"Coinbase", "FROMSYMBOL":"BTC", "TOSYMBOL":"GBP", "FLAGS":2, "PRICE":36400.58, "LASTUPDATE":1648585450, "LASTVOLUME":0.00149048, "LASTVOLUMETO":54.2543364784, "LASTTRADEID":"29522101", "VOLUMEDAY":425.32474119, "VOLUMEDAYTO":15444921.4999973, "VOLUME24HOUR":511.19211209, "VOLUME24HOURTO":18554055.2809571, "VOLUMEHOUR":1.43854872, "VOLUMEHOURTO":52358.6923852105 }"""
        )

        json.forEach {
            val item = JsonBridge.deserialize<CryptoCompareWsResponse>(it)
            println(item)
        }
    }
}