package com.uleaf.client

import com.uleaf.client.client.ClientService

class ULeafClient

fun main(args: Array<String>) {
    ClientService.boot("127.0.0.1", 9999)
}