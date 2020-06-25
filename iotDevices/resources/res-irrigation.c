#include "contiki.h"
#include "coap-engine.h"
#include <string.h>
#include "time.h"
#include "os/dev/leds.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "App"
#define LOG_LEVEL LOG_LEVEL_DBG

bool irrigation_state = 0;

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

RESOURCE(res_irrigation,
         "title=\"Irrigation controller: ?POST/PUT state=ON|OFF\";obs;rt=\"Humidity Control\"",
         res_get_handler,
         res_post_put_handler,
         res_post_put_handler,
         NULL);

static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  if(request != NULL) 
    LOG_DBG("GET Request Sent\n");

	LOG_DBG("STATE: %d\n", irrigation_state);

	unsigned int accept = -1;
  coap_get_header_accept(request, &accept);
	
	if (accept == -1)
		accept = APPLICATION_JSON;

	if(accept == APPLICATION_XML) {
		coap_set_header_content_format(response, APPLICATION_XML);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "<state=\"%d\"/>", irrigation_state);
		coap_set_payload(response, buffer, strlen((char *)buffer));
    
	} else if(accept == APPLICATION_JSON) {
		coap_set_header_content_format(response, APPLICATION_JSON);
		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"state\":%d}", irrigation_state);
		coap_set_payload(response, buffer, strlen((char *)buffer));
  
	} else {
		coap_set_status_code(response, NOT_ACCEPTABLE_4_06);
		const char *msg = "Supporting content-type application/json";
		coap_set_payload(response, msg, strlen(msg));
  }
}

static void res_post_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	if(request != NULL) {
		LOG_DBG("POST/PUT Request Sent\n");
		LOG_DBG("Actual state: %d\n", irrigation_state);
	}
  
	size_t len = 0;
	const char *state = NULL;
	int success = 1;

	if((len = coap_get_post_variable(request, "state", &state))) {
		if(strncmp(state, "ON", len) == 0) {
			irrigation_state = 1;
			LOG_DBG("Irrigation Started! \n");
			leds_set(LEDS_NUM_TO_MASK(LEDS_GREEN));
		} else if(strncmp(state, "OFF", len) == 0) {
			irrigation_state = 0;
			LOG_DBG("Irrigation Stopped \n");
			leds_set(LEDS_NUM_TO_MASK(LEDS_RED));
		} else
			success = 0;
	} else 
		success = 0;
  
	if(success)
		coap_set_status_code(response, CHANGED_2_04);
	else
		coap_set_status_code(response, BAD_REQUEST_4_00);
		
}
