//
// temp.swift
//
//
//  Created by Keyur on 2/16/18.
//  Copyright © 2018 Keyur. All rights reserved.
//

import Foundation
import EVReflection



class UserModel :EVObject, Codable{
    var Id:String!
    var Hash:String! // added by me
    var Email : String!
    var Password : String!
    var PreferredName : String!
    var FirstName : String!
    var LastName : String!
    var BirthDate : String!
    var AccountPhoto : String!
    var Tel : String!
    var Mobile : String!
    var BillingAddressId : Int!
    var BillingAddress : MyAddressModel!
    var MainAddressId : Int!
    var MainAddress : MyAddressModel!
    var FacebookId : String!
    var GoogleId : String!
    var TwitterId : String!
    var Description : String!
    var IsBusiness : Bool!
    var BusinessName : String!
    var ABN : String!
    var ACN : String!
    var Credit : Int!
    var FeeTier:Int!
    var PositiveReview:Int!
    var Referral : String!
    var NotificationSettings:[String:String]!
    var AvailableTimes:MyAvailableTimeModelList!
    var AutoAcceptRequest:Bool!
    var CreatedDateTimeSydney : String!
    var Pending : Bool!
    var Suspended : Bool!
    var Deleted : Bool!
    var PendingReason : [String]! // added by me
    var currLocation:LocationModel!  // added by me
    
    required init() {
        Hash = ""
        PendingReason = [String]()
        currLocation = LocationModel.init()
    }
    
    func getUserDetailToSave() -> UserModel? {
        if let id = self.Id{
            if self.Hash != ""{
                if let email = self.Email{
                    var user:UserModel = UserModel.init()
                
                    user.Id = id
                    user.Hash = Hash
                    user.Email = email
                    user.currLocation = AppModel.shared.currentUser.currLocation
                    return user
                }
            }
        }
        return nil
    }
}



