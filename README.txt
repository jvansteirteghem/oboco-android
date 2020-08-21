icons: https://github.com/google/material-design-icons
grey600: https://github.com/google/material-design-icons/issues/110

debug app on hardware device
	open samsung tablet
	enable developer mode
		go to settings, about tablet, software information and tap build numer 7 times.
	enable usb debugging
		go to settings, developer options, allow usb debugging
	install oem usb drivers
		https://developer.android.com/studio/run/oem-usb#Drivers
		install samsung drivers
	open android studio
		select run, debug app, connected device