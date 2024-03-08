package com.example.jmb_bms.connectionService

import android.os.Build
import java.math.BigInteger
import java.security.SecureRandom
import kotlin.math.sqrt


/*
class encryptor {

    private lateinit var mod : BigInteger
    private lateinit var base: BigInteger
    private val exponent: BigInteger
    private val finalExp: BigInteger


    private fun isPrime(num: BigInteger): Boolean{
        val two: BigInteger = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            BigInteger.TWO // Available from API level  33
        } else {
            BigInteger("2") // Compatible with older Android versions
        }
        if(num.compareTo(two) < 0) return false
        if(num == two || num == BigInteger.valueOf(3)) return true
        if(num.mod(two) == BigInteger("0")) return false

        val limit = BigInteger.valueOf(sqrt(num.toDouble()).toLong())

        var i = BigInteger.valueOf(3)

        while ( i < limit)
        {
            if( num.mod(i) == BigInteger("0")) return false
            i = i.add(BigInteger("1"))
        }
        return true
    }

    private fun correctOrder(p: BigInteger, q: BigInteger, g: BigInteger): Boolean
    {
        if (!p.isProbablePrime(100)) {
            return false // p must be a prime number
        }

        val order = p.subtract(BigInteger.ONE) // p-1
        val gToTheOrder = g.modPow(order, p)
        if (gToTheOrder != BigInteger.ONE) {
            return false // g is not a primitive root modulo p
        }

        return true
    }


    private fun prepareValues()
    {
        val secureRandom = SecureRandom()

        var q = BigInteger.probablePrime(256,secureRandom)
        while (! isPrime(q)){
            q = BigInteger.probablePrime(256,secureRandom)
        }

        var p : BigInteger
        var g : BigInteger

        do{
            val r = BigInteger.probablePrime(256, secureRandom)
            p = q.multiply(r).add(BigInteger.ONE)
            while (!isPrime(p)) {
                p = q.multiply(r).add(BigInteger.ONE)
            }

            var h = BigInteger.probablePrime(256, secureRandom)
            g = h.multiply(p).mod(p)
            while (g == BigInteger.ONE) {
                h = BigInteger.probablePrime(256, secureRandom)
                g = h.multiply(p).mod(p)
            }
        } while (correctOrder(p, q, g))

        base = g
        mod = p
        //exponent = 1..(q.subtract(BigInteger.ONE)).ran
    }

    init {
        prepareValues()
    }

}

 */