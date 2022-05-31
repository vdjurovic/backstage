package co.bitshifted.backstage

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackstageApplication

fun main(args: Array<String>) {
	runApplication<BackstageApplication>(*args)
}
