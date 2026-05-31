#!/bin/bash

emulator -avd Pixel_8a -accel on -gpu swiftshader_indirect -no-snapshot "${@}"
