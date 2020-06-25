#include "contiki.h"

#include <stdio.h>
#include <string.h>
#include "time.h"
#include "coap-engine.h"
#include "coap-observe.h"
#include "os/dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_DBG

#define LOWER_THRESHOLD		10
#define UPPER_THRESHOLD		30

#define PERIODIC_HANDLER_INTERVAL 10

static int humidity_value = 15;
extern bool irrigation_state;
static int counter = 0;

int generate_random_humidity(int lower, int upper) 
{ 
	int num = (rand() %  (upper - lower + 1)) + lower; 
	return num;
} 

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

EVENT_RESOURCE(res_humidity,
   "title=\"Soil Humidity\";obs;rt=\"Humidity Sensor\"",
   res_get_handler,
   NULL,
   NULL,
   NULL,
	 res_event_handler);

static void res_event_handler(void)
{
	counter ++;
	coap_notify_observers(&res_humidity);
}

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  if(request != NULL) {
    LOG_DBG("Observing Handler Number %d\n", counter);
  }
  
  humidity_value = generate_random_humidity(0, 100);

	if(humidity_value < LOWER_THRESHOLD){
		if(!irrigation_state){
			LOG_DBG("Irrigation started automatically\n");
			irrigation_state = 1;
			leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
		}
	} else if(humidity_value > UPPER_THRESHOLD){
		if(irrigation_state){
			LOG_DBG("Irrigation stopped automatically\n");
			irrigation_state = 0;
			leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
		}
	}

  unsigned int accept = -1;
  coap_get_header_accept(request, &accept);
	
	if (accept == -1)
		accept = APPLICATION_JSON;

  if(accept == APPLICATION_XML) {
    coap_set_header_content_format(response, APPLICATION_XML);
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "<humidity=\"%d\"/>", humidity_value);
    coap_set_payload(response, buffer, strlen((char *)buffer));
    
  } else if(accept == APPLICATION_JSON) {
    coap_set_header_content_format(response, APPLICATION_JSON);
    snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"humidity\":%d}", humidity_value);
    coap_set_payload(response, buffer, strlen((char *)buffer));
    
  } else {
    coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
    const char *msg = "Supporting content-type application/json";
    coap_set_payload(response, msg, strlen(msg));
  }
}
