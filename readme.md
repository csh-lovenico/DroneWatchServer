# DroneWatchServer
CSE 594: Design and Implementation of a Real-time Drone View Publishing System

This is the backend of *DroneWatch*.

Note that this is not an out-of-the-box program. It is integrated with Firebase, and you need to take extra steps to configure the project.

## Requirements

* Java >= 11.0
* If you use Eclipse, You need to install Lombok plugin.

## Before running

* Follow [this documentation](https://firebase.google.com/docs/admin/setup?authuser=0#initialize-sdk) to initialize Firebase Admin SDK and set environment variables.
* Set up MongoDB and Redis, then modify the configuration file.

## Components
* Controller: HTTP APIs
* Service: Business procedures
* Repository: Database operations
* Entity: Data objects used in this system
