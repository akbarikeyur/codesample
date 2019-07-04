//
//  GlobalConstant.swift
//
//
//  Created by Keyur on 9/22/17.
//  Copyright © 2017 Keyur. All rights reserved.
//

import Foundation
import UIKit

let BASE_URL = "https://api.toshare.com.au/"

struct STORYBOARD{
    static var SETTINGS = UIStoryboard(name: "Settings", bundle: nil)
    static var LOGIN = UIStoryboard(name: "Login", bundle: nil)
    static var HOME = UIStoryboard(name: "Home", bundle: nil)
    static var BOOKING = UIStoryboard(name: "Booking", bundle: nil)
}

struct Platform {
    static var isSimulator: Bool {
        return TARGET_OS_SIMULATOR != 0
    }
}

//"yyyy-MM-dd HH:mm:ss Z"

struct DATE_FORMAT {
    static let DATE = "dd-MM-yyyy"
    static let DEF_CARD_EXP_DATE = "MM/yyyy"
    static let DISP_CARD_EXP_DATE = "MM/yy"
    static let DISP_VOUCHER_EXP_DATE = "dd-MM-yyyy"
    static let SERVER_DATE = "yyyy-MM-dd'T'HH:mm:ss"
}

struct TIME_FORMAT {
    static let DISP_PICKUPTIME_TIME = "HH:mm"
}

struct CONSTANT{
    
    static let MAX_EMAIL_CHAR = 254
    static let MAX_PREFER_NAME_CHAR = 40
    static let MIN_PWD_CHAR = 6
    static let MAX_FIRST_NAME_CHAR = 40
    static let MAX_LAST_NAME_CHAR = 40
    
    static let DOB_CHAR = 8
    static let DOB_SPACE_CHAR = 4
    
    static let MOBILE_NUMBER_CHAR = 8
    static let MOBILE_NUMBER_SPACE_CHAR = 2
    static let MOBILE_NUMBER_CODE = "04"
    
    static let CARD_NUMBER_CHAR = 16
    static let CARD_NUMBER_DASH_CHAR = 3
    static let CARD_EXP_DATE_CHAR = 5
    static let CARD_CVV_CHAR = 3
    
    static let SMS_CODE_CHAR = 4
    static let SMS_CODE_SPACE_CHAR = 3
    
    static let DP_IMAGE_WIDTH     =  200
    static let DP_IMAGE_HEIGHT    =  200
    static let IMAGE_QUALITY   =  1
    
    static let CURRENCY   =  "$"
    static let DIST_MEASURE   =  "km"
    static let TIME_ZONE = "Australia/Sydney"
 
    static let DEF_TAKE:Int = 24
    
    static let GOOGLE_MAP_KEY:String = "AIzaSyDfYUO6G4B7lb13FDnv1WtX64GqOzwqX8I"
}

struct HOME_CONSTANT{
    
    
    static let DEF_IS_AD:Bool = true
    
    static let DEF_KEYWORD:String = ""
    static let CATEGORY:[String] = ["All", "Tools", "Trailers", "Caravans", "Outdoors"]
    static let DEF_CATEGORY:Int = 0
    static let ORDER:[String] = ["Best Match", "Lowest Price / 24 Hr", "Highest Price / 24 Hr", "Distance"]
    static let ORDER_SERVER:[String] = ["bestmatch", "price", "price", "distance"]
    static let DEF_ORDER:Int = 0
    static let DEF_RADIUS:Int = 3
    //static let STATE:[String] = ["Australia", "Australian Capital Territory", "New south wales", "Northern Territory", "Queensland", "South Australia", "Tasmania", "Vitoria", "Western Australia"]
    static let RADIUS:[String] = ["5", "15", "30", "50", "200"] // km
    
    //Tool
    static let TOOL_POWERED_BY:[String] = ["All", "Battery", "Electric cord power", "Petrol", "Disel", "Non-Power", "Air"]
    static let DEF_TOOL_POWERED_BY:Int = 0
    static let TOOL_PURPOSE:[String] = ["All", "Generic", "Woodworking", "Plastering", "Concrete, Tile & Brick", "Cleaning", "Earthmoving", "Gardening & Landscaping", "Test & Measuring", "Safty, Site & Access", "Plumbing", "Cooling & Heating"]
    static let DEF_TOOL_PURPOSE:Int = 0
    
    //Trailer
    static let TRAILER_SIZE:[String] = ["All", "6' X 4'", "7' X 5'", "8' X 5'", "8' X 6'", "10' X 6'", "Other"]
    static let DEF_TRAILER_SIZE:Int = 0
    static let TRAILER_TYPE:[String] = ["All", "Plain", "Caged", "Enclosed", "Flatbed trailer", "Car Carrier", "Horse Float", "Bike Trailer", "Tipper Trailer", "Other"]
    static let DEF_TRAILER_TYPE:Int = 0
    static var BRAKE_TYPE:BrakeTypeModelList = BrakeTypeModelList.init()
    static let DEF_BRAKE_TYPE:Int = 0
    static var CONNECTOR_TYPE:ConnectorTypeModelList =  ConnectorTypeModelList.init()
    static let DEF_CONNECTOR_TYPE:Int = 0
    
    //Caravan
    static let CARAVAN_SLEEPS:[String] = ["All", "2+", "3+", "4+", "5+", "6+"]
    static let CARAVAN_SLEEPS_SERVER:[String] = ["All", "2", "3", "4", "5", "6"]
    
    static let DEF_CARAVAN_SLEEPS:Int = 0
    static let CARAVAN_TYPES:[String] = ["All", "Caravan", "Camper Trailer", "Campervan & Motorhome"]
    static let DEF_CARAVAN_TYPES:Int = 0
    static let CARAVAN_AMENITIES:[String] = ["All caravans", "Annex", "Awning", "Outdoor Kitchen or BBQ", "Kitchen", "Fridge", "Microwave", "TV", "Air conditioner", "Heating", "Bathroom", "Linen Towels", "Camping table", "Camping chairs", "Reversing Camera"]
    static let DEF_CARAVAN_AMENITIES:Int = 0
    static let CARAVAN_PET_FRIENDLY:[String] = ["All", "Yes", "No"]
    static let DEF_CARAVAN_PET_FRIENDLY:Int = 0
    
    //Outdoors
    static let OUTDOORS_PURPOSE:[String] = ["All", "Backyard Toys", "Aquatic Toys", "Other Outdoor Equiptment"]
    static let DEF_OUTDOORS_PURPOSE:Int = 0
    
    static var REFINE_SELECTION:[[String:[Any]]] = [["Categories":CATEGORY], ["Sort By":ORDER], ["Location":[]], //0,1,2
                                                    ["Power by":TOOL_POWERED_BY], ["Purpose":TOOL_PURPOSE], //3,4
                                                    ["Trailer size":TRAILER_SIZE], ["Trailer type":TRAILER_TYPE], ["Brake type":[]], ["Connector type":[]], //5,6,7,8
                                                    ["Sleeps":CARAVAN_SLEEPS], ["Caravan type":CARAVAN_TYPES], ["Brake type":[]], ["Connector type":[]] , ["Amenities":CARAVAN_AMENITIES], ["Pet friendly":CARAVAN_PET_FRIENDLY], // 9,10,11,12,13,14
                                                    ["Purpose":OUTDOORS_PURPOSE]] //15
    
}
    
struct PENDING_REASON {
    static let AWAITING_FOR_VERIFICATION:String = "AwaitingForVerification"
    static let MOBILE_NOT_VALID:String = "MobileNotValid"
    static let PHOTO_ID_NOT_VALID:String = "PhotoIdNotValid"
    static let NO_LINKED_CREDIT_CARD:String = "NoLinkedCreditCard"
}

struct NOTIFICATION
{
    static let ON_UPDATE_USER_DETAIL =  "ON_UPDATE_USER_DETAIL"
    static let ON_UPDATE_USER_LOCATION =  "ON_UPDATE_USER_LOCATION"
    static let ON_OPEN_SEARCH_REFINE_SELECTION_VIEW =  "ON_OPEN_SEARCH_REFINE_SELECTION_VIEW"
    static let ON_UPDATE_SEARCH_REFINE_PARA =  "ON_UPDATE_SEARCH_REFINE_PARA"
    static let ON_UPDATE_SEARCH_RESULT =  "ON_UPDATE_SEARCH_RESULT"
}
