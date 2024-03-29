/**
 *  StatHoliday or Not
 *
 *  Copyright 2021 Ham
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  V0.1 Initial Release
 *  V0.2 Changed state typeOfDate to state Today. Added checking for what tomorrow is. Added abilitly to disable debug logging.
 *  V0.3 Added optional Truth and Reconciliation day (Sept 30)
 */
 
metadata {
	definition (name: "Stat Holiday Switch", namespace: "hamsando", author: "ham") { 
        capability "Switch"
        capability "Refresh"       
		capability "Initialize"
		attribute "TodayIs", "string"
		attribute "TomorrowIs", "string"
		
		command "on"
		command "off"
	}

    preferences {
		input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false
		input name: "celebrateEasterMonday", type: "bool", title: "Easter Monday?", defaultValue: false
        input name: "honorTruthReconciliation", type: "bool", title: "Truth & Reconciliation Day?", defaultValue: false
    }
}

def installed() {
	logDebug "Installed with settings: ${settings}"	
    initialize()
}

def refresh() {
    updated()
}

def updated() {
	logDebug "Updated with settings: ${settings}"
    initialize()	    
}

def initialize() {	 
    off()    
	state.clear()
	state.Today = "Work day"
	state.Tomorrow = "Work day"
	
    try {
        unsubscribe()
    } catch (unsubError) {
        logDebug(unsubError)
    } 
    
    try {
        unschedule()
    } catch (unschError) {
        logDebug(unschError)
    }
    schedule("0 5 0 1/1 * ? *", doDaily)	
    doDaily()
}

def on() {
    sendEvent(name: "switch", value: "on", isStateChange: true)	    
}

def off() {
    sendEvent(name: "switch", value: "off", isStateChange: true)	    
}

def doDaily() {
	if (todayIsAHoliday(0))  {    
    	log.info "Today is not a work day!"
        on()
    } else {
    	log.info "Today is a work day"
        off()
	}

	todayIsAHoliday(1)
    log.info "Tomorrow is: " + state.Tomorrow
}

def todayIsAHoliday(int tomorrow) {
    notFound = true
    dayType = "Workday" 
    
    todayTomorrow = "Tomorrow"
    if (tomorrow == 0) {
        todayTomorrow = "Today"
    }
    
	def thisDay = new Date().clearTime() + tomorrow
    logDebug(todayTomorrow + " is: " + thisDay)
        
	if (isWeekend(thisDay)) {
        dayType = "Weekend"
        notFound = false
    } else {
    	if (notFound && isNYD(thisDay)) {
            dayType = "New Years Day"
            notFound = false
        }
        
        if (notFound && isFD(thisDay)) {
			dayType = "Family Day"
        	notFound = false
        }

		if (notFound && isGF(thisDay)) {
        	dayType = "Good Friday"
			notFound = false
        }

		if (notFound && (celebrateEasterMonday) && isEM(thisDay)) {
        	dayType = "Easter Monday"
			notFound = false
        }

        if (notFound && isVD(thisDay)) {
            dayType = "Victoria Day"
        	notFound = false
        }

        if (notFound && isCD(thisDay)) {
        	dayType = "Canada Day"        
        	notFound = false
        }

        if (notFound && isBC(thisDay)) {
        	dayType = "BC Day"        
        	notFound = false
        }

        if (notFound && isLD(thisDay)) {
        	dayType = "Labor Day"        
        	notFound = false
        }

        if (notFound && (honorTruthReconciliation) && isTruthDay(thisDay)) {
        	dayType = "Truth & Reconciliation Day"
			notFound = false
        }


        if (notFound && isT(thisDay)) {
        	dayType = "Thanksgiving"        
        	notFound = false
        }

        if (notFound && isRD(thisDay)) {
        	dayType = "Rememberance Day"        
        	notFound = false
        }

        if (notFound && isX(thisDay)) {
        	dayType = "Xmas"        
        	notFound = false
        }

        if (notFound && isBD(thisDay)) {
        	dayType = "Boxing Day"        
        	notFound = false
        }        
    }
	
	if (tomorrow == 0) {
		state.Today = dayType
		sendEvent(name: "TodayIs", value: dayType, displayed:true, isStateChange: true)
	} else {
		state.Tomorrow = dayType
		sendEvent(name: "TomorrowIs", value: dayType, displayed:true, isStateChange: true)
	}
	
    return !notFound
}

def isWeekend(Date d) {
	logDebug "Checking weekend"
	def df = new java.text.SimpleDateFormat("EEEE")
    def dayOfWeek = df.format(d)    	
    if ((dayOfWeek=="Saturday")|| (dayOfWeek=="Sunday")) 
    	return true
    else 
    	return false
       
}

def isNYD(Date d) {
	logDebug "Checking New Years Day"    
	//if (d == findFollowingMonday(new Date(d[Calendar.YEAR], 0, 1))) {
    if (d == findFollowingMonday(new Date().parse('MM/dd/yyyy', "01/01/" + d[Calendar.YEAR]))) {
    	return true
    } else {
    	return false    
    }
}

def isFD(Date d) {
	logDebug "Checking family day"
	if (d == findxDay(3, "Monday",2)) 
    	return true
     else 
    	return false       	
}

def isGF(Date cd) {	
	logDebug "Checking Good Friday"
    int y = cd[Calendar.YEAR]
    int c = (y/100)
    int n = y - 19 * (y/19).toInteger()
	int k = (( c - 17 ) / 25)
    int i = c - (c / 4) - (( c - k ) / 3).toInteger() + 19 * n + 15;
    i = i - 30 * ( i / 30 ).toInteger();
    i = i - ( i / 28 ).toInteger() * ( 1 - ( i / 28 ).toInteger() * ( 29 / ( i + 1 ) ).toInteger() * ( ( 21 - n ) / 11 ).toInteger());
    int j = y + (y / 4).toInteger() + i + 2 - c + (c / 4).toInteger();
    j = j - 7 * ( j / 7 ).toInteger()
    int l = i - j;
    int m = 3 + (( l + 40 ) / 44).toInteger();
    int d = l + 28 - 31 * ( m / 4 ).toInteger();    
	
	Date tmpDate =  new Date(y-1900,m-1,d)	
	logDebug "Easter is $tmpDate"
	tmpDate = tmpDate-2
	logDebug "Good Friday is $tmpDate"
    if (cd == tmpDate) 
    	return true
    else
    	return false
}

def isEM(Date cd) {
	logDebug "Checking Easter Monday"
    int y = cd[Calendar.YEAR]
    int c = (y/100)
    int n = y - 19 * (y/19).toInteger()
	int k = (( c - 17 ) / 25)
    int i = c - (c / 4) - (( c - k ) / 3).toInteger() + 19 * n + 15;
    i = i - 30 * ( i / 30 ).toInteger();
    i = i - ( i / 28 ).toInteger() * ( 1 - ( i / 28 ).toInteger() * ( 29 / ( i + 1 ) ).toInteger() * ( ( 21 - n ) / 11 ).toInteger());
    int j = y + (y / 4).toInteger() + i + 2 - c + (c / 4).toInteger();
    j = j - 7 * ( j / 7 ).toInteger()
    int l = i - j;
    int m = 3 + (( l + 40 ) / 44).toInteger();
    int d = l + 28 - 31 * ( m / 4 ).toInteger();    
	
	Date tmpDate =  new Date(y-1900,m-1,d)	
	logDebug "Easter is $tmpDate"
	tmpDate = tmpDate+1
	logDebug "Easter Monday is $tmpDate"
    if (cd == tmpDate) 
    	return true
    else
    	return false
}

def isVD(Date d) {
	logDebug "Checking Victoria Day"
	def day = ""
	def Z=0    
    def year = d[Calendar.YEAR]
    def df = new java.text.SimpleDateFormat("EEEE")
    df.setTimeZone(location.timeZone)
    
	for (int x=1; x <= 7; x++) {
    	Z = 25-x    	
    	day = df.format(new Date().parse('MM/dd/yyyy', "5/$Z/$year"))        
        if (day == "Monday") {        
        	if (d == new Date().parse('MM/dd/yyyy', "5/$Z/$year")) 
        		return true
            else 
            	return false
        }
        
    }
}

def isCD(Date d) {
	logDebug "Checking Canada Day"
    if (d == findFollowingMonday(new Date().parse('MM/dd/yyyy', "07/01/" + d[Calendar.YEAR]))) 
    	return true
    else
    	return false    
}

def isBC(Date d) {
	logDebug "Checking BC Day"
	if (d == findxDay(1, "Monday",8)) 
    	return true
     else 
    	return false       	
}

def isLD(Date d) {
	logDebug "Checking Labor Day"
	if (d == findxDay(1, "Monday",9)) 
    	return true
     else 
    	return false       	
}

def isTruthDay(Date d) {
	logDebug "Checking Truth & Reconciliation Day"
    if (d == findFollowingMonday(new Date().parse('MM/dd/yyyy', "09/30/" + d[Calendar.YEAR]))) 
	//if (d == findFollowingMonday(new Date(d[Calendar.YEAR], 11, 11))) 
    	return true
    else
    	return false    
}


def isT(Date d) {
	logDebug "Checking Thanksgiving"
	if (d == findxDay(2, "Monday",10)) 
    	return true
     else 
    	return false       	
}

def isRD(Date d) {
	logDebug "Checking Rememberance Day"
    if (d == findFollowingMonday(new Date().parse('MM/dd/yyyy', "11/11/" + d[Calendar.YEAR]))) 
	//if (d == findFollowingMonday(new Date(d[Calendar.YEAR], 11, 11))) 
    	return true
    else
    	return false    
}

def isX(Date d) {
	logDebug "Checking Xmas"
    if (d == findFollowingMonday(new Date().parse('MM/dd/yyyy', "12/25/" + d[Calendar.YEAR]))) 
	//if (d == findFollowingMonday(new Date(d[Calendar.YEAR], 12, 25))) 
    	return true
    else
    	return false    
}

def isBD(Date d) {
	logDebug "Checking Boxing Day"
    def bd = findFollowingMonday(new Date().parse('MM/dd/yyyy', "12/25/" + d[Calendar.YEAR])) + 1
    //def bd = findFollowingMonday(new Date(d[Calendar.YEAR], 12, 25)) + 1 
	if (d == findFollowingMonday(bd)) 
    	return true
    else
    	return false    
}

def findFollowingMonday(Date d) {
	logDebug "  Checking following Monday for " + d
	def df = new java.text.SimpleDateFormat("EEEE")
    // Ensure the new date object is set to local time zone
    df.setTimeZone(location.timeZone)
    def day = df.format(d)
    logDebug("  Day of the week of actual holiday: " + day)
    if (day == "Saturday") {
        logDebug("  Day we celebrate on: " + (d+2))
    	return d+2
    } else if (day == "Sunday") {
        logDebug("  Day we celebrate on: " + (d+1))
    	return d+1
    } else {
        logDebug("  Day we celebrate on: " + (d))
    	return d
    }            
}

def findxDay(int nth, String dow, int month) {
	logDebug "  Checking $nth $dow of month $month"
    def currentDate = new Date()
    
    def year = currentDate[Calendar.YEAR]
    def futureMonth = month+1    
	def lastDayOfMonth = new Date().parse('MM/dd/yyyy', "$futureMonth/1/$year") - 1    
    //logDebug "Last Day of Month: $lastDayOfMonth[Calendar.DATE]"
    
    def day = 1
    def counter=0    
    def tmpDate = new Date()
    def df = new java.text.SimpleDateFormat("EEEE")
    def dayOfWeek
    for (int d=day; d <= lastDayOfMonth[Calendar.DATE]; d++) {
		tmpDate = new Date().parse('MM/dd/yyyy', "$month/$d/$year")
        //logDebug tmpDate        
    	dayOfWeek = df.format(tmpDate)   
        //logDebug dayOfWeek
    	if (dayOfWeek==dow) {
        	counter++
            if (counter == nth) {
                logDebug("  Day we celebrate on: " + tmpDate)
            	return tmpDate
            }
        }        
    }
    return false
}

def logDebug(value){
    if (logEnable) log.debug(value)
}
