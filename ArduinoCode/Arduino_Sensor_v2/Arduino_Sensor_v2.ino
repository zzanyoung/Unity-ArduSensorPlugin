#include <SparkFun_SGP30_Arduino_Library.h>

#include <Wire.h>
#include <SPI.h>
#include <SoftwareSerial.h>
#include <Adafruit_Sensor.h>
#include "Adafruit_BME680.h"

#define BME_SCK 13
#define BME_MISO 12
#define BME_MOSI 11
#define BME_CS 10

#define SEALEVELPRESSURE_HPA (1013.25)
#define MAX_LENGTH 20

//Adafruit_BME680 bme; // I2C
Adafruit_BME680 bme(BME_CS); // hardware SPI
//Adafruit_BME680 bme(BME_CS, BME_MOSI, BME_MISO,  BME_SCK);

SGP30 sgpSensor;

SoftwareSerial ssSerial = SoftwareSerial(4, 5);

void setup() {
  Serial.begin(9600);
  ssSerial.begin(9600);
  
  while (!Serial);
  Serial.println(F("BME680 test"));

  if (!bme.begin()) {
    Serial.println("Could not find a valid BME680 sensor, check wiring!");
    while (1);
  }

  // Set up oversampling and filter initialization
  bme.setTemperatureOversampling(BME680_OS_8X);
  bme.setHumidityOversampling(BME680_OS_2X);
  bme.setPressureOversampling(BME680_OS_4X);
  bme.setIIRFilterSize(BME680_FILTER_SIZE_3);
  bme.setGasHeater(320, 150); // 320*C for 150 ms

  Wire.begin();
  if (sgpSensor.begin() == false) {
    Serial.println("No SGP30 Detected.");
  }

  sgpSensor.initAirQuality();
}

void loop() {
  if (! bme.performReading()) {
    Serial.println("Failed to perform reading :(");
    return;
  }
  Serial.print("Temperature = ");
  Serial.print(bme.temperature);
  Serial.println(" *C");

  Serial.print("Pressure = ");
  Serial.print(bme.pressure / 100.0);
  Serial.println(" hPa");

  Serial.print("Humidity = ");
  Serial.print(bme.humidity);
  Serial.println(" %");

  Serial.print("Gas = ");
  Serial.print(bme.gas_resistance / 1000.0);
  Serial.println(" KOhms");

  Serial.print("Approx. Altitude = ");
  Serial.print(bme.readAltitude(SEALEVELPRESSURE_HPA));
  Serial.println(" m");

  Serial.println();

  Serial.println("--------------SGP30------------------\n");
  delay(1000);
  sgpSensor.measureAirQuality();
  Serial.print("CO2: ");
  Serial.print(sgpSensor.CO2);
  Serial.print(" ppm\tTVOC: ");
  Serial.print(sgpSensor.TVOC);
  Serial.println(" ppb");

  // Send data 
  char env_data[256];
  char f0[MAX_LENGTH], f1[MAX_LENGTH], f2[MAX_LENGTH], f3[MAX_LENGTH], f4[MAX_LENGTH], f5[MAX_LENGTH], f6[MAX_LENGTH];
  
  dtostrf(bme.temperature, 6, 2, f0);
  dtostrf(bme.humidity, 6, 2, f1);
  dtostrf(bme.pressure / 100.0, 6, 2, f2);
  dtostrf(bme.gas_resistance / 1000.0, 6, 2, f3);
  dtostrf(bme.readAltitude(SEALEVELPRESSURE_HPA), 6, 2, f4);
  dtostrf(sgpSensor.CO2, 6, 2, f5);
  dtostrf(sgpSensor.TVOC, 6, 2, f6);

  sprintf(env_data, "%s,%s,%s,%s,%s,%s,%s", f0, f1, f2, f3, f4, f5, f6);
  ssSerial.println(env_data);
  
  delay(1000);
}
