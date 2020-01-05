/**
 *  StatHoliday or Not
 *
 *  Copyright 2019 Ham
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
 */
 
metadata {
	definition (name: "Stat Holiday Switch", namespace: "hamsando", author: "ham") { 
        capability "Switch"
        capability "Refresh"       
		capability "Initialize"
		command "on"
		command "off"        
	}

    preferences {
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"	
    initialize()
}

def refresh() {
    updated()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    initialize()	    
}

def initialize() {	 
    //def tz = TimeZone.getTimeZone('America/Los_Angeles') 
	//def rundate = d.format('dd/MM/yyyy HH:mm', tz)
    off()    
    state.typeOfDate = "Workday"
    try {
        unsubscribe()
    } catch (unsubError) {
        log.info(unsubError)
    } 
    
    try {
        unschedule()
    } catch (unschError) {
        log.info(unschError)
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
	if (todayIsAHoliday())  {    
    	log.debug "Today is not a work day!"
        on()
    } else {
    	log.debug "Today is a work day"
        off()
	}

}

def todayIsAHoliday() {
	def thisDay = new Date().clearTime()    
    log.debug("Today is: " + thisDay)
    state.typeOfDate = "Workday"
	if (isWeekend(thisDay)) {
    	state.typeOfDate = "Weekend"
    	return true
    } else {
    	if (isNYD(thisDay)) {
        	state.typeOfDate = "New Years Day"
        	return true
        }
        
        if (isFD(thisDay)) {
        	state.typeOfDate = "Family Day"
        	return true
        }

		if (isGF(thisDay)) {
        	state.typeOfDate = "Good Friday"
        	return true
        }

        if (isVD(thisDay)) {
            state.typeOfDate = "Victoria Day"
        	return true
        }

        if (isCD(thisDay)) {
        	state.typeOfDate = "Canada Day"        
        	return true
        }

        if (isBC(thisDay)) {
        	state.typeOfDate = "BC Day"        
        	return true
        }

        if (isLD(thisDay)) {
        	state.typeOfDate = "Labor Day"        
        	return true
        }

        if (isT(thisDay)) {
        	state.typeOfDate = "Thanksgiving"        
        	return true
        }

        if (isRD(thisDay)) {
        	state.typeOfDate = "Rememberance Day"        
        	return true
        }

        if (isX(thisDay)) {
        	state.typeOfDate = "Xmas"        
        	return true
        }

        if (isBD(thisDay)) {
        	state.typeOfDate = "Boxing Day"        
        	return true
        }        
    }
    return false
}

def isWeekend(Date d) {
	log.debug "Checking weekend"
	def df = new java.text.SimpleDateFormat("EEEE")
    def dayOfWeek = df.format(d)    	
    if ((dayOfWeek=="Saturday")|| (dayOfWeek=="Sunday")) 
    	return true
    else 
    	return false
       
}

def isNYD(Date d) {
	log.debug "Checking New Years Day"    
	//if (d == findFollowingMonday(new Date(d[Calendar.YEAR], 0, 1))) {
    if (d == findFollowingMonday(new Date().parse('MM/dd/yyyy', "01/01/" + d[Calendar.YEAR]))) {
    	return true
    } else {
    	return false    
    }
}

def isFD(Date d) {
	log.debug "Checking family day"
	if (d == findxDay(3, "Monday",2)) 
    	return true
     else 
    	return false       	
}

def isGF(Date cd) {	
	log.debug "Checking Good Friday"
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
	log.debug "Easter is $tmpDate"
	tmpDate = tmpDate-2
	log.debug "Good Friday is $tmpDate"
    if (cd == tmpDate) 
    	return true
    else
    	return false
}

def isVD(Date d) {
	log.debug "Checking Victoria Day"
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
	log.debug "Checking Canada Day"
    if (d == findFollowingMonday(new Date().parse('MM/dd/yyyy', "07/01/" + d[Calendar.YEAR]))) 
    	return true
    else
    	return false    
}

def isBC(Date d) {
	log.debug "Checking BC Day"
	if (d == findxDay(1, "Monday",8)) 
    	return true
     else 
    	return false       	
}

def isLD(Date d) {
	log.debug "Checking Labor Day"
	if (d == findxDay(1, "Monday",9)) 
    	return true
     else 
    	return false       	
}

def isT(Date d) {
	log.debug "Checking Thanksgiving"
	if (d == findxDay(2, "Monday",10)) 
    	return true
     else 
    	return false       	
}

def isRD(Date d) {
	log.debug "Checking Rememberance Day"
    if (d == findFollowingMonday(new Date().parse('MM/dd/yyyy', "11/11/" + d[Calendar.YEAR]))) 
	//if (d == findFollowingMonday(new Date(d[Calendar.YEAR], 11, 11))) 
    	return true
    else
    	return false    
}

def isX(Date d) {
	log.debug "Checking Xmas"
    if (d == findFollowingMonday(new Date().parse('MM/dd/yyyy', "12/25/" + d[Calendar.YEAR]))) 
	//if (d == findFollowingMonday(new Date(d[Calendar.YEAR], 12, 25))) 
    	return true
    else
    	return false    
}

def isBD(Date d) {
	log.debug "Checking Boxing Day"
    def bd = findFollowingMonday(new Date().parse('MM/dd/yyyy', "12/25/" + d[Calendar.YEAR])) + 1
    //def bd = findFollowingMonday(new Date(d[Calendar.YEAR], 12, 25)) + 1 
	if (d == findFollowingMonday(bd)) 
    	return true
    else
    	return false    
}

def findFollowingMonday(Date d) {
	log.debug "  Checking following Monday for " + d
	def df = new java.text.SimpleDateFormat("EEEE")
    // Ensure the new date object is set to local time zone
    df.setTimeZone(location.timeZone)
    def day = df.format(d)
    log.debug("  Day of the week of actual holiday: " + day)
    if (day == "Saturday") {
        log.debug("  Day we celebrate on: " + (d+2))
    	return d+2
    } else if (day == "Sunday") {
        log.debug("  Day we celebrate on: " + (d+1))
    	return d+1
    } else {
        log.debug("  Day we celebrate on: " + (d))
    	return d
    }            
}

def findxDay(int nth, String dow, int month) {
	log.debug "  Checking $nth $dow of month $month"
    def currentDate = new Date()
    
    def year = currentDate[Calendar.YEAR]
    def futureMonth = month+1    
	def lastDayOfMonth = new Date().parse('MM/dd/yyyy', "$futureMonth/1/$year") - 1    
    //log.debug "Last Day of Month: $lastDayOfMonth[Calendar.DATE]"
    
    def day = 1
    def counter=0    
    def tmpDate = new Date()
    def df = new java.text.SimpleDateFormat("EEEE")
    def dayOfWeek
    for (int d=day; d <= lastDayOfMonth[Calendar.DATE]; d++) {
		tmpDate = new Date().parse('MM/dd/yyyy', "$month/$d/$year")
        //log.debug tmpDate        
    	dayOfWeek = df.format(tmpDate)   
        //log.debug dayOfWeek
    	if (dayOfWeek==dow) {
        	counter++
            if (counter == nth) {
                log.debug("  Day we celebrate on: " + tmpDate)
            	return tmpDate
            }
        }        
    }
    return false
}
