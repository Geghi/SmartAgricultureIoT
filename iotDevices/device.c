#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "contiki.h"
#include "coap-engine.h"
#include "coap-blocking-api.h"
#include "random.h"
#include "node-id.h"
#include "os/dev/leds.h"

/* Log configuration */
#include "coap-log.h"
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_DBG

#define TRIGGER_INTERVAL	10

#define SERVER_EP "coap://[fd00::1]:5683"

extern coap_resource_t res_irrigation;
extern coap_resource_t res_humidity;
extern bool irrigation_state;
bool registered = false;

PROCESS(node_process, "node");
AUTOSTART_PROCESSES(&node_process);

static struct etimer timer;

/* This function is will be passed to COAP_BLOCKING_REQUEST() to handle responses. */
void client_chunk_handler(coap_message_t *response)
{
	const uint8_t *chunk;

	if(response == NULL) {
		LOG_INFO("Request timed out");
		return;
	}
	
	int len = coap_get_payload(response, &chunk);
	registered = true;	

	LOG_INFO("|%.*s \n", len, (char *)chunk);
}

PROCESS_THREAD(node_process, ev, data){

	static coap_endpoint_t server_ep;
	static coap_message_t request[1];

	PROCESS_BEGIN();

	leds_set(LEDS_NUM_TO_MASK(LEDS_RED));	

	coap_activate_resource(&res_irrigation, "res_irrigation");
	coap_activate_resource(&res_humidity, "res_humidity");

	coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

	coap_init_message(request, COAP_TYPE_CON, COAP_GET, 0);
	coap_set_header_uri_path(request, "registration");

	LOG_DBG("Sending Registration Request\n");
	COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

	while(!registered){
		LOG_DBG("Registration retry...\n");
		COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);
	}
	etimer_set(&timer, CLOCK_SECOND * TRIGGER_INTERVAL);

	while(true) {
		PROCESS_WAIT_EVENT();
		
		if(ev == PROCESS_EVENT_TIMER && data == &timer){	
			res_humidity.trigger();
			etimer_reset(&timer);
		}
	}

  PROCESS_END();
}
