
//
//  AppDelegate.swift
//  Check-Up
//
//  Created by Keyur on 09/08/17.
//  Copyright © 2017 Keyur. All rights reserved.
//

import UIKit
import Firebase
import FirebaseAuth
import FirebaseDatabase

import FBSDKCoreKit
import FBSDKLoginKit
import IQKeyboardManagerSwift
import CoreLocation
import GoogleMaps
import GooglePlacePicker

import Alamofire
import NVActivityIndicatorView
import Social
import Fabric
import Crashlytics

import GoogleMobileAds
import UserNotifications
import FirebaseMessaging

import CoreData
import Foundation
import MessageUI


@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, customAlertDelegate, CLLocationManagerDelegate, URLSessionDelegate, MFMailComposeViewControllerDelegate {

    var window: UIWindow?
    
    var appUsersRef:DatabaseReference!
    var appUsersRefHandler:UInt = 0;

    
    var eventsRef:DatabaseReference!
    var eventsRefHandler:UInt = 0;

    var courtRef:DatabaseReference!
    var courtRefHandler:UInt = 0

    var inboxListRef : DatabaseReference!
    var inboxListRefHandler:UInt = 0
    var inboxNewMessageNoti : [String : Bool] = [String : Bool] ()
    
    var messageListRef : DatabaseReference!
    var messageListRefHandler:UInt = 0
    
    var storyListRef : DatabaseReference!
    var storyListRefHandler:UInt = 0
    
    var alertController : UIAlertController!
    
    var alert : customAlertView!
    var locationManager: CLLocationManager!
    
    var oldLatitude : Float!
    var oldLongitde : Float!
    
    var activityLoader : NVActivityIndicatorView!
    
    var userFcmToken : String = ""
    var localView : LocalPushNotificationView!
    
    var sentStoryToFriendBadges : [String : [[String : Any]]] = [String : [[String : Any]]] ()
    
    var userPopup : UserProfileDialogView? = nil
    var isStoryFromGallary : Bool = false
    
    var _isGotMyData:Bool = false
    var _isSetAllHandler:Bool = false
    var _isShowMapLoader:Bool = false
    var _isGetAllUsers:Bool = false
    var _isGetAllEvents:Bool = false
    var _isGetAllCourts:Bool = false
    var _isGetAllStory:Bool = false
    var _isGetInboxList:Bool = false
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool{
        // Override point for customization after application launch.
        
        NotificationCenter.default.addObserver(self, selector: #selector(self.keyboardDidShow), name: .UIKeyboardDidShow, object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(self.keyboardDidHide), name: .UIKeyboardDidHide, object: nil)
        
        //firebase configuration
        FirebaseApp.configure()
        GMSServices.provideAPIKey(GOOGLE.KEY)
        GMSPlacesClient.provideAPIKey(GOOGLE.KEY)
        
        Preference.sharedInstance.setDataToPreference(data: false as AnyObject, forKey: "isLastSeenUpdate")
        
        //create Table
        appUsersRef = Database.database().reference().child("USERS")
        eventsRef = Database.database().reference().child("EVENTS")
        courtRef = Database.database().reference().child("COURTS")
        inboxListRef = Database.database().reference().child("INBOX")
        storyListRef = Database.database().reference().child("STORY")
        messageListRef = Database.database().reference().child("MESSAGES")

        FBSDKApplicationDelegate.sharedInstance().application(application, didFinishLaunchingWithOptions: launchOptions)
        
        if let user = Preference.sharedInstance.getUserLoginData(){
            AppModel.shared.currentUser = UserModel.init(dict: user);
        }
        
        oldLatitude = 0.0
        oldLongitde = 0.0
        setUserAuthStateHandler()
        
        IQKeyboardManager.sharedManager().enable = true

        if currentUserId().count == 0
        {
            logout()
        }
        
        Fabric.with([Crashlytics.self])
        GADMobileAds.configure(withApplicationID: GOOGLE.ADMOB)

        addCrashDetail()
        
        registerPushNotification(application)
        
        return true
    }
    
    func keyboardDidShow(_ notif:NSNotification){
        AppModel.shared.isKeyboardOpen = true
    }
    
    func keyboardDidHide(_ notif:NSNotification){
        AppModel.shared.isKeyboardOpen = false 
    }
    func addCrashDetail()
    {
        Crashlytics.sharedInstance().setUserIdentifier(UIDevice.current.identifierForVendor?.uuidString)
        Crashlytics.sharedInstance().setUserEmail("amishapadasala20@gmail.com")
        Crashlytics.sharedInstance().setUserName("Amisha")
    }
    func storyboard() -> UIStoryboard
    {
        return UIStoryboard(name: "Main", bundle: nil)
    }
    func sharedDelegate() -> AppDelegate
    {
        return UIApplication.shared.delegate as! AppDelegate
    }
    
    
    func callAllHandler()
    {
        setLocationManager()
        appUsersHandler()
        storyListHandler()
        allCourtHandler()
        allEventsHandler()
        inboxListHandler()
    }
    func setAllHandler(){
        if(_isShowMapLoader == false){
            window?.isUserInteractionEnabled = false
            displayLoader()
        }
        _isShowMapLoader = true
        if(_isSetAllHandler == true || _isGetAllUsers == false || _isGetAllEvents == false || _isGetAllCourts == false || _isGetAllStory == false || _isGetInboxList == false){
            return
        }
        _isSetAllHandler = true
        removeLoader()
        window?.isUserInteractionEnabled = true
    }
    
    //MARK:- FCM other func
    func currentUserId() -> String {
        return Auth.auth().currentUser?.uid ?? ""
    }
    func updateCurrentUserData()
    {
        if(_isGotMyData){
            Preference.sharedInstance.setUserLoginData(dict: AppModel.shared.currentUser.dictionary())
            appUsersRef.child(AppModel.shared.currentUser.uID).setValue(AppModel.shared.currentUser.dictionary())
        }
    }
    func updateLastSeen(isOnline : Bool)
    {
        if AppModel.shared.currentUser != nil
        {
            if AppModel.shared.currentUser.uID.count > 0
            {
                if Preference.sharedInstance.getDataFromPreference(key: "isLastSeenUpdate") != nil && Preference.sharedInstance.getDataFromPreference(key: "isLastSeenUpdate") as! Bool == true
                {
                    if isOnline
                    {
                        AppModel.shared.currentUser.last_seen = ""
                        updateCurrentUserData()
                    }
                    else
                    {
                        AppModel.shared.currentUser.last_seen = getCurrentDateInString()
                        updateCurrentUserData()
                    }
                }
            }
        }
    }
    
    //MARK: FCM Handler
    func setUserAuthStateHandler(){
        Auth.auth().addStateDidChangeListener { (auth, user) in
            if(user == nil){
                self.navigateToSignInSignUp()
            }
            else{
                if(AppModel.shared.currentUser == nil){
                    self.logout()
                }
                else{
                    AppModel.shared.currentUser.uID = self.currentUserId()
                    
                    if user?.isEmailVerified == false  && AppModel.shared.currentUser.login_type == USER.EMAIL_LOGIN
                    {
                        user?.sendEmailVerification(completion: { (error) in
                            if error != nil
                            {
                                displayToast((error?.localizedDescription)!)
                                self.logout()
                            }
                            else
                            {
                                displayToast("Please verify your email address.")
                            }
                            
                        })
                    }
                    self.navigateToDashBoard()
                }
               
            }
        }
    }
    func appUsersHandler()
    {
        
        appUsersRef.removeObserver(withHandle: appUsersRefHandler)
      
        appUsersRefHandler = appUsersRef.observe(DataEventType.value) { (snapshot : DataSnapshot) in
            self._isGetAllUsers = true
            AppModel.shared.USERS = [UserModel]()
            var isCurrUserExist:Bool = false
            if snapshot.exists()
            {
                for child in snapshot.children {
                    
                    let user:DataSnapshot = child as! DataSnapshot
                    if let userDict = user.value as? [String : AnyObject]{
                        if AppModel.shared.validateUser(dict: userDict){
                            let userModel = UserModel.init(dict: userDict)
                            if( AppModel.shared.currentUser != nil && AppModel.shared.currentUser.uID == user.key)
                            {
                                AppModel.shared.currentUser = userModel
                                self.uploadUserProfileImage()
                                isCurrUserExist = true
                            }
                            else
                            {
                                AppModel.shared.USERS.append(userModel)
                            }
                        }
                    }
                }
            }
            if AppModel.shared.currentUser != nil && self._isGotMyData == false{
                self._isGotMyData = true
                AppModel.shared.currentUser.fcmToken = AppDelegate().sharedDelegate().userFcmToken
                AppModel.shared.currentUser.last_seen = ""
                AppDelegate().sharedDelegate().updateCurrentUserData()
                
                if AppModel.shared.currentUser.email == ""
                {
                    NotificationCenter.default.post(name:NSNotification.Name(rawValue: NOTIFICATION.SHOW_PROFILE_SCREEN), object: nil)
                }
            }
            if(isCurrUserExist  == true)
            {
                self.onUpdateAllUser()
                self.onUpdateBadgeCount()
            }
            
            self.setAllHandler()
        }
    }
    
    func allEventsHandler()
    {
        eventsRef.removeObserver(withHandle: eventsRefHandler)
    
        eventsRefHandler = eventsRef.observe(DataEventType.value) { (snapshot : DataSnapshot) in
            self._isGetAllEvents = true
            AppModel.shared.EVENTS = [EventModel]()
            if snapshot.exists()
            {
                for child in snapshot.children {
                    let events:DataSnapshot = child as! DataSnapshot
                    if let eventDict = events.value as? [String : AnyObject]{
                        if AppModel.shared.validateEvent(dict: eventDict){
                            let eventModel = EventModel.init(dict: eventDict)
                            AppModel.shared.EVENTS.append(eventModel)
                        }
                    }
                }
            }
            self.onUpdateEvents()
            self.setAllHandler()
        }
    }
   
  
    func allCourtHandler()
    {
        courtRef.removeObserver(withHandle: courtRefHandler)

        courtRefHandler = courtRef.observe(DataEventType.value) { (snapshot : DataSnapshot) in
            self._isGetAllCourts = true
            AppModel.shared.COURTS = [CourtModel]()
            if snapshot.exists()
            {
                for child in snapshot.children {
                    
                    let courts:DataSnapshot = child as! DataSnapshot
                    if let courtDict = courts.value as? [String : AnyObject]{
                        if AppModel.shared.validateCourt(dict: courtDict){
                            let courtModel = CourtModel.init(dict: courtDict)
                            AppModel.shared.COURTS.append(courtModel)
                        }
                    }
                }
            }
            self.checkforCheckInCheckOut()
            self.onUpdateCourts()
            self.setAllHandler()
        }
    }
    
    func checkforCheckInCheckOut(){
        if(AppModel.shared.currentUser != nil && AppModel.shared.currentUser.curr_court != ""){
            let index = AppModel.shared.COURTS.index(where: { (temp) -> Bool in
                temp.location.id == AppModel.shared.currentUser.curr_court
            })
            if(index == nil || AppModel.shared.currentUser.courts.index(of:AppModel.shared.currentUser.curr_court) == nil) {
                self.onCheckOutCourt()
            }
            else{
                AppModel.shared.CURRENT_CHECKIN_COURT = AppModel.shared.COURTS[index!]
                let lastCourtCoordinate = CLLocation(latitude: CLLocationDegrees(AppModel.shared.CURRENT_CHECKIN_COURT.location.latitude), longitude: CLLocationDegrees(AppModel.shared.CURRENT_CHECKIN_COURT.location.longitude))
                
                let distanceInMeters = lastCourtCoordinate.distance(from: CLLocation(latitude: CLLocationDegrees(Preference.sharedInstance.getUserLatitude()), longitude: CLLocationDegrees(Preference.sharedInstance.getUserLongitude()))) // result is in meters
                
                if Preference.sharedInstance.getUserLatitude() != 0 && Preference.sharedInstance.getUserLongitude() != 0 && distanceInMeters > CLLocationDistance(USERVALUE.LEAVING_DIFFERENCE)
                {
                    self.onCheckOutCourt()
                }
            }
        }
        else{
            AppModel.shared.CURRENT_CHECKIN_COURT = nil
        }
    }
    
    
    func inboxListHandler()
    {
        inboxListRef.removeObserver(withHandle: inboxListRefHandler)
        inboxListRefHandler = inboxListRef.observe(DataEventType.value) { (snapshot : DataSnapshot) in
            self._isGetInboxList = true
            AppModel.shared.INBOXLIST = [InboxListModel]()
            if snapshot.exists()
            {
                var arrNewMsg : [String] = [String] ()
                for child in snapshot.children {
                    let channel:DataSnapshot = child as! DataSnapshot
                    if let channelDict = channel.value as? [String : AnyObject]{
                        if AppModel.shared.validateInbox(dict: channelDict){
                            let msgList : InboxListModel = InboxListModel.init(dict: channelDict)
                            AppModel.shared.INBOXLIST.append(msgList)
                            
                            if msgList.lastMessage.status == 2 && self.inboxNewMessageNoti[msgList.lastMessage.msgID] == nil && msgList.lastMessage.connectUserID == self.currentUserId()
                            {
                                if let otherUser : UserModel = self.getConnectUserDetail(channelId: msgList.id)
                                {
                                    msgList.lastMessage.status = 3
                                    self.inboxNewMessageNoti[msgList.lastMessage.msgID] = true
                                    arrNewMsg.append(msgList.id)
                                    
                                    let vc : UIViewController = UIApplication.topViewController()!
                                    if (vc is ChatViewController) && (vc as! ChatViewController).channelId == msgList.id {
                                    }
                                    else
                                    {
                                        if #available(iOS 10.0, *) {
                                            self.showLocalPush(title: "New Message", subTitle: otherUser.name + ((msgList.lastMessage.text.decodeString != "") ? (" : " + msgList.lastMessage.text.decodeString) : " has sent story."), user: otherUser)
                                        } else {
                                            // Fallback on earlier versions
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for i in 0..<arrNewMsg.count
                {
                    self.inboxListRef.child(arrNewMsg[i]).child("lastMessage").child("status").setValue(3)
                }
            }
            self.onUpdateInbox()
            self.onUpdateBadgeCount()
            self.setAllHandler()
        }
    }
    
    func storyListHandler()
    {
        storyListRef.removeObserver(withHandle: storyListRefHandler)

        storyListRefHandler = storyListRef.observe(DataEventType.value) { (snapshot : DataSnapshot) in
            self._isGetAllStory = true
            AppModel.shared.STORY = [String : StoryModel]()
            if snapshot.exists()
            {
                for child in snapshot.children {
                    let channel:DataSnapshot = child as! DataSnapshot
                    if let channelDict = channel.value as? [String : AnyObject]{
                        if AppModel.shared.validateStory(dict: channelDict){
                            let story : StoryModel = StoryModel.init(dict: channelDict)
                            AppModel.shared.STORY[channel.key] = story
                        }
                    }
                }
            }
            self.onUpdateStories()
            self.setAllHandler()
        }
    }
        
    //MARK: EVENT
    func createEvent(_ event : EventModel)
    {
        let newEventRef : DatabaseReference = eventsRef.childByAutoId()
        event.id = newEventRef.key
        newEventRef.setValue(event.dictionary())
        
        //add to court from prelocated court
        let index1 = AppModel.shared.PRELOCATED_COURTS.index(where: { (location) -> Bool in
            location.id == event.location.id
        })
        
        if index1 != nil
        {
            AppModel.shared.PRELOCATED_COURTS.remove(at: index1!)
        }
        let index2 = AppModel.shared.COURTS.index { (courtModel) -> Bool in
            courtModel.location.id == event.location.id
        }
        if(index2 == nil)
        {
            
            let tempCourt : CourtModel = CourtModel.init(location: event.location, players: [], activity: [], comment: [], story: [], date: getCurrentDateInString(), type: 1, uID : AppModel.shared.currentUser.uID)
            if index1 == nil
            {
                tempCourt.type = 2
            }
            AppModel.shared.COURTS.append(tempCourt)
            courtRef.child(event.location.id).setValue(tempCourt.dictionary())
        }
        
        let index = AppModel.shared.COURTS.index { (courtModel) -> Bool in
            courtModel.location.id == event.location.id
        }
        
        if index != nil
        {
            let courtModel:CourtModel = AppModel.shared.COURTS[index!]
            courtModel.activity.append(event.id)
            courtRef.child(event.location.id).child("activity").setValue(courtModel.activity)
        }
    
        for i in 0..<event.players.count
        {
            let index = AppModel.shared.USERS.index(where: { (user) -> Bool in
                user.uID == event.players[i].id
            })
            if index != nil
            {
                sendPush(title: "", body: AppModel.shared.currentUser.name + " has invited you to join " + event.title , user: AppModel.shared.USERS[index!], type: PUSH_NOTIFICATION.TYPE.EVENT_INVITE, otherId: event.id)
            }
        }
    }
    
    func updateEvent(_ oldEvent : EventModel, updatedEvent : EventModel)
    {
        if oldEvent.location.id != updatedEvent.location.id
        {
            let index = AppModel.shared.COURTS.index(where: { (tempCourt) -> Bool in
                tempCourt.location.id == oldEvent.location.id
            })
            
            if index != nil
            {
                let index1 = AppModel.shared.COURTS[index!].activity.index(where: { (tempEvent) -> Bool in
                    tempEvent == oldEvent.id
                })
                
                if index1 != nil
                {
                    AppModel.shared.COURTS[index!].activity.remove(at: index1!)
                    courtRef.child(oldEvent.location.id).child("activity").child(String(index1!)).removeValue()
                }
            }
        }
     
        eventsRef.child(updatedEvent.id).setValue(updatedEvent.dictionary())
        
        //add to court from prelocated court
        let index1 = AppModel.shared.PRELOCATED_COURTS.index(where: { (location) -> Bool in
            location.id == updatedEvent.location.id
        })
        
        if index1 != nil
        {
            AppModel.shared.PRELOCATED_COURTS.remove(at: index1!)
        }
        let index2 = AppModel.shared.COURTS.index { (courtModel) -> Bool in
            courtModel.location.id == updatedEvent.location.id
        }
        if(index2 == nil)
        {
            let tempCourt : CourtModel = CourtModel.init(location: updatedEvent.location, players: [], activity: [], comment: [], story: [], date: getCurrentDateInString(), type: 1, uID : AppModel.shared.currentUser.uID)
            if index1 == nil
            {
                tempCourt.type = 2
            }
            AppModel.shared.COURTS.append(tempCourt)
            courtRef.child(updatedEvent.location.id).setValue(tempCourt.dictionary())
        }
            
        let index = AppModel.shared.COURTS.index { (courtModel) -> Bool in
            courtModel.location.id == updatedEvent.location.id
        }
        
        if index != nil
        {
            let courtModel:CourtModel = AppModel.shared.COURTS[index!]
            
            let index3 = courtModel.activity.index(where: { (tempEvent) -> Bool in
                tempEvent == updatedEvent.id
            })
            if index3 == nil
            {
                courtModel.activity.append(updatedEvent.id)
                courtRef.child(updatedEvent.location.id).child("activity").setValue(courtModel.activity)
            }
        }
        
        
        var newPlayers:[ContactModel] = [ContactModel]()
        var oldPlayers:[ContactModel] = oldEvent.players
        var updatedPlayers:[ContactModel] = [ContactModel]()
        
        for i in 0..<updatedEvent.players.count
        {
            let index = oldPlayers.index(where: { (tempContact) -> Bool in
                tempContact.id == updatedEvent.players[i].id
            })
            
            if index == nil
            {
                newPlayers.append(updatedEvent.players[i])
            }
            else
            {
                updatedPlayers.append(updatedEvent.players[i])
                oldPlayers.remove(at: index!)
            }
        }
        
        for i in 0..<newPlayers.count
        {
            let index = AppModel.shared.USERS.index(where: { (user) -> Bool in
                user.uID == newPlayers[i].id
            })
            if index != nil
            {
                sendPush(title: "", body: AppModel.shared.currentUser.name + " has invited you to join " + updatedEvent.title , user: AppModel.shared.USERS[index!], type: PUSH_NOTIFICATION.TYPE.EVENT_INVITE, otherId: updatedEvent.id)
            }
        }
        
        for i in 0..<oldPlayers.count
        {
            let index = AppModel.shared.USERS.index(where: { (user) -> Bool in
                user.uID == oldPlayers[i].id
            })
            if index != nil
            {
                sendPush(title: "", body: AppModel.shared.currentUser.name + " removed you from " + oldEvent.title , user: AppModel.shared.USERS[index!], type: PUSH_NOTIFICATION.TYPE.EVENT_INVITE, otherId: oldEvent.id)
            }
        }
        
        for i in 0..<updatedPlayers.count
        {
            let index = AppModel.shared.USERS.index(where: { (user) -> Bool in
                user.uID == updatedPlayers[i].id
            })
            if index != nil
            {
                sendPush(title: "", body: AppModel.shared.currentUser.name + " updated " + oldEvent.title , user: AppModel.shared.USERS[index!], type: PUSH_NOTIFICATION.TYPE.EVENT_INVITE, otherId: updatedEvent.id)
            }
        }
    }
    
    func joinEvent(_ event : EventModel)
    {
        let index = event.players.index { (tempContact) -> Bool in
            tempContact.id == AppModel.shared.currentUser.uID
        }
        if index != nil
        {
            event.players[index!].requestAction = 3
            eventsRef.child(event.id).child("players").child(String(index!)).child("requestAction").setValue(3)
        }
        else
        {
            event.players.append(ContactModel.init(id: AppModel.shared.currentUser.uID, requestAction: 3))
            eventsRef.child(event.id).child("players").setValue(AppModel.shared.getContactArrOfDictionary(arr: event.players))
        }
        let index1 = AppModel.shared.USERS.index(where: { (user) -> Bool in
            user.uID == event.uID
        })
        if index1 != nil{
            
            sendPush(title: "", body: AppModel.shared.currentUser.name + " has joined " + event.title , user: AppModel.shared.USERS[index1!], type: PUSH_NOTIFICATION.TYPE.EVENT_JOIN, otherId: event.id)
        }
    }
    
    func removeEventInvitation(_ event : EventModel)
    {
        //remove from current user
        
        if event.uID == AppModel.shared.currentUser.uID
        {
            for i in 0..<AppModel.shared.COURTS.count
            {
                let index = AppModel.shared.COURTS[i].activity.index(where: { (tempEvent) -> Bool in
                    tempEvent == event.id
                })
                
                if index != nil
                {
                    AppModel.shared.COURTS[i].activity.remove(at: index!)
                    courtRef.child(AppModel.shared.COURTS[i].location.id).child("activity").child(String(index!)).removeValue()
                    break
                }
            }
            eventsRef.child(event.id).removeValue()
            
            for i in 0..<event.players.count
            {
                let index1 = AppModel.shared.USERS.index { (tempUser) -> Bool in
                    tempUser.uID == event.players[i].id
                }
                
                if index1 != nil
                {
                    sendPush(title: "", body: AppModel.shared.currentUser.name + " has cancelled " + event.title , user: AppModel.shared.USERS[index1!], type: PUSH_NOTIFICATION.TYPE.EVENT_CANCELLED, otherId: "")
                }
            }
        }
        else
        {
            let index = event.players.index { (tempPlayer) -> Bool in
                tempPlayer.id == AppModel.shared.currentUser.uID
            }
            
            if index != nil
            {
                event.players.remove(at: index!)
                eventsRef.child(event.id).child("players").child(String(index!)).removeValue()
                let index1 = AppModel.shared.USERS.index { (tempUser) -> Bool in
                    tempUser.uID == event.uID
                }
                
                if index1 != nil
                {
                    sendPush(title: "", body: AppModel.shared.currentUser.name + " has declined to join " + event.title , user: AppModel.shared.USERS[index1!], type: PUSH_NOTIFICATION.TYPE.EVENT_DECLINED, otherId: event.id)
                }
            }
        }
    }
    func addMoreUserToEvent(_ arrUser : [UserModel], event : EventModel)
    {
        let oldEvent:EventModel = EventModel.init(dict: event.dictionary())
        for i in 0..<arrUser.count
        {
            let user : UserModel = arrUser[i]
            event.players.append(ContactModel.init(id: user.uID, requestAction: 1))
        }
        updateEvent(oldEvent, updatedEvent: event)
        //eventsRef.child(event.id).child("players").setValue(AppModel.shared.getContactArrOfDictionary(arr: event.players))
    }
    func addCommentToEvent(_ comment : CommentModel, event : EventModel)
    {
        event.comment.append(comment)
        self.eventsRef.child(event.id).child("comment").setValue(AppModel.shared.getCommentArrOfDictionary(arr: event.comment))
        
        if event.uID != currentUserId()
        {
            let index = AppModel.shared.USERS.index(where: { (tempUser) -> Bool in
                tempUser.uID == event.uID
            })
            if index != nil
            {
                sendPush(title: event.title, body: AppModel.shared.currentUser.name + " add new comment.", user: AppModel.shared.USERS[index!], type: PUSH_NOTIFICATION.TYPE.EVENT_COMMENT, otherId: event.id)
            }
        }
        
        for i in 0..<event.players.count
        {
            if event.players[i].requestAction == 3
            {
                let index = AppModel.shared.USERS.index(where: { (tempUser) -> Bool in
                    tempUser.uID == event.players[i].id
                })
                if index != nil
                {
                    sendPush(title: event.title, body: AppModel.shared.currentUser.name + " add new comment.", user: AppModel.shared.USERS[index!], type: PUSH_NOTIFICATION.TYPE.EVENT_COMMENT, otherId: event.id)
                }
            }
        }
    }
    
    //MARK: COURT
    func onCheckedInCourt(_ court : CourtModel)
    {
        let index0 = AppModel.shared.COURTS.index { (courtModel) -> Bool in
            courtModel.location.id == court.location.id
        }
        if(index0 == nil)
        {
            AppModel.shared.COURTS.append(court)
        }
        
        AppModel.shared.CURRENT_CHECKIN_COURT = CourtModel.init(dict: court.dictionary())
        AppModel.shared.currentUser.curr_court = court.location.id
        AppModel.shared.currentUser.total_checkIn = AppModel.shared.currentUser.total_checkIn + 1
        updateCurrentUserData()
        
        let index = AppModel.shared.currentUser.courts.index { (locationId) -> Bool in
            locationId == court.location.id
        }
        
        if(index == nil){
            AppModel.shared.currentUser.courts.append(court.location.id)
            updateCurrentUserData()
        }
        
        for i in 0..<AppModel.shared.EVENTS.count
        {
            let event : EventModel = AppModel.shared.EVENTS[i]
            if event.location.id == court.location.id
            {
                let index = court.activity.index(where: { (tempActivity) -> Bool in
                    tempActivity == event.id
                })
                if index == nil
                {
                    court.activity.append(event.id)
                }
            }
        }
        
        //if exist then create new court , otherwise overwrite
        courtRef.child(court.location.id).setValue(court.dictionary())
        
        //send push
        for i in 0..<AppModel.shared.currentUser.contact.count
        {
            let index = AppModel.shared.USERS.index(where: { (user) -> Bool in
                user.uID == AppModel.shared.currentUser.contact[i].id
            })
            if index != nil
            {
                sendPush(title: "", body: AppModel.shared.currentUser.name + " has just checked-in at " + court.location.name , user: AppModel.shared.USERS[index!], type: PUSH_NOTIFICATION.TYPE.CHECKED_IN, otherId: court.location.id)
            }
        }
    }
    func onCheckOutCourt(_ isLogout:Bool = false)
    {
        if AppModel.shared.currentUser != nil && AppModel.shared.currentUser.curr_court != ""
        {
            let index = AppModel.shared.COURTS.index(where: { (tempCourt) -> Bool in
                tempCourt.location.id == AppModel.shared.currentUser.curr_court
            })
            if index != nil
            {
                let index1 = AppModel.shared.COURTS[index!].players.index(where: { (tempPlayer) -> Bool in
                    tempPlayer == AppModel.shared.currentUser.uID
                })
                if index1 != nil
                {
                    AppModel.shared.COURTS[index!].players.remove(at: index1!)
                    courtRef.child(AppModel.shared.COURTS[index!].location.id).child("players").child(String(index1!)).removeValue()
                }
            }
            
            let index2 = AppModel.shared.currentUser.courts.index(where: { (tempCourt1) -> Bool in
                tempCourt1 == AppModel.shared.currentUser.curr_court
            })
            if index2 != nil
            {
                AppModel.shared.currentUser.courts.remove(at: index2!)
                updateCurrentUserData()
            }
        }
        
        AppModel.shared.CURRENT_CHECKIN_COURT = nil
        AppModel.shared.currentUser.curr_court = ""
        updateCurrentUserData()
        if(isLogout == false){
            
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: NOTIFICATION.CHECK_FOR_CHECKEDIN), object: nil)
        }
    }
    func onCreateCourt(_ court : CourtModel) // Ploting pin on map using place picker
    {
        let index0 = AppModel.shared.COURTS.index { (courtModel) -> Bool in
            courtModel.location.id == court.location.id
        }
        if(index0 == nil)
        {
            AppModel.shared.COURTS.append(court)
        }
        
        for i in 0..<AppModel.shared.EVENTS.count
        {
            let event : EventModel = AppModel.shared.EVENTS[i]
            if event.location.id == court.location.id
            {
                let index = court.activity.index(where: { (tempActivity) -> Bool in
                    tempActivity == event.id
                })
                if index == nil
                {
                    court.activity.append(event.id)
                }
            }
        }
        
        //if exist then create new court , otherwise overwrite
        courtRef.child(court.location.id).setValue(court.dictionary())
    }
    func onPrelocatedCourtTap(_ court : CourtModel)
    {
        let index0 = AppModel.shared.COURTS.index { (courtModel) -> Bool in
            courtModel.location.id == court.location.id
        }
        if(index0 == nil)
        {
            AppModel.shared.COURTS.append(court)
        }
        for i in 0..<AppModel.shared.EVENTS.count
        {
            let event : EventModel = AppModel.shared.EVENTS[i]
            if event.location.id == court.location.id
            {
                court.activity.append(event.id)
            }
        }
        
        //if exist then create new court , otherwise overwrite
        courtRef.child(court.location.id).setValue(court.dictionary())
    }
    func addCommentToCourt(_ comment : CommentModel, court : CourtModel)
    {
        court.comment.append(comment)
        self.courtRef.child(court.location.id).child("comment").setValue(AppModel.shared.getCommentArrOfDictionary(arr: court.comment))
        
        
        for i in 0..<court.players.count
        {
            let index = AppModel.shared.USERS.index(where: { (tempUser) -> Bool in
                tempUser.uID == court.players[i]
            })
            if index != nil
            {
                sendPush(title: court.location.name, body: AppModel.shared.currentUser.name + " add new comment.", user: AppModel.shared.USERS[index!], type: PUSH_NOTIFICATION.TYPE.COURT_COMMENT, otherId: court.location.id)
            }
        }
        
        if court.uID != currentUserId()
        {
            let index = court.players.index(where: { (tempUser) -> Bool in
                tempUser == court.uID
            })
            if index == nil
            {
                let index1 = AppModel.shared.USERS.index(where: { (tempUser) -> Bool in
                    tempUser.uID == court.uID
                })
                if(index1 != nil){
                    sendPush(title: court.location.name, body: AppModel.shared.currentUser.name + " add new comment.", user: AppModel.shared.USERS[index1!], type: PUSH_NOTIFICATION.TYPE.COURT_COMMENT, otherId: court.location.id)
                }
            }
        }
    }
   
    
    func removeCreatedCourt(_ court : CourtModel,index:Int = -1)
    {
        //remove all activity
        for strEvent in court.activity
        {
            eventsRef.child(strEvent).removeValue()
        }
        
        //remove current user's checkedIn Court
        if(AppModel.shared.currentUser != nil){
            let index1 = AppModel.shared.currentUser.courts.index { (temp) -> Bool in
                temp == court.location.id
            }
            if(index1  != nil){
                AppModel.shared.currentUser.courts.remove(at: index1!)
                updateCurrentUserData()
            }
        }
        //remove checkIn court from other user
        for i in 0..<court.players.count
        {
            let index2 = AppModel.shared.USERS.index(where: { (temp) -> Bool in
                temp.uID == court.players[i]
            })
            if(index2 != nil){
                let index3 = AppModel.shared.USERS[index2!].courts.index(where: { (temp) -> Bool in
                    temp == court.location.id
                })
                if(index3 != nil){
                    AppModel.shared.USERS[index2!].courts.remove(at: index3!)
                }
            }

        }
        if(index == -1){
            let index4 = AppModel.shared.COURTS.index(where: { (temp) -> Bool in
                temp.location.id == court.location.id
            })
            if(index4 != nil){
                AppModel.shared.COURTS.remove(at: index4!)
            }
        }
        courtRef.child(court.location.id).removeValue()
        
        if(court.location.id == AppModel.shared.currentUser.curr_court){
            onCheckOutCourt()
        }
    }
    
    //MARK: FRIEND
    func sendFriendRequest(_ user : UserModel)
    {
        let index = AppModel.shared.currentUser.contact.index { (tempContact) -> Bool in
            tempContact.id == user.uID
        }
        
        if index != nil
        {
            AppModel.shared.currentUser.contact.remove(at: index!)
            updateCurrentUserData()
            let index1 = user.contact.index { (tempContact) -> Bool in
                tempContact.id == AppModel.shared.currentUser.uID
            }
            
            if index1 != nil
            {
                user.contact.remove(at: index1!)
                appUsersRef.child(user.uID).child("contact").child(String(index1!)).removeValue()
            }
            displayToast("Request Cancelled.")
        }
        else
        {
            var contact : ContactModel = ContactModel.init(id: user.uID, requestAction: 1) // send request
            AppModel.shared.currentUser.contact.append(contact)
            updateCurrentUserData()
            
            let index = user.contact.index(where: { (tempContact) -> Bool in
                tempContact.id == AppModel.shared.currentUser.uID
            })
            
            if index != nil
            {
                user.contact.remove(at: index!)
            }
            
            contact = ContactModel.init(id: AppModel.shared.currentUser.uID, requestAction: 2) //got current user request
            user.contact.append(contact)
            appUsersRef.child(user.uID).child("contact").setValue(AppModel.shared.getContactArrOfDictionary(arr: user.contact))
            
            sendPush(title: "", body: AppModel.shared.currentUser.name + " sent you friend request", user: user, type: PUSH_NOTIFICATION.TYPE.FRIEND_REQUEST)
            displayToast("Request Sent.")
        }
    }
    func addFriend(_ user : UserModel)
    {
        let index = AppModel.shared.currentUser.contact.index { (contact) -> Bool in
            contact.id == user.uID
        }
        
        if index != nil {
            AppModel.shared.currentUser.contact[index!].requestAction = 3 // Aceept Reuest
            
            //add current user from other user contact
            
            let index1 = AppModel.shared.USERS.index { (otherUser) -> Bool in
                otherUser.uID == AppModel.shared.currentUser.contact[index!].id
            }
            if(index1 != nil){
                let index2 = AppModel.shared.USERS[index1!].contact.index { (otherContact) -> Bool in
                    otherContact.id == AppModel.shared.currentUser.uID
                }
                if(index2 != nil){
                    AppModel.shared.USERS[index1!].contact[index2!].requestAction = 3 // Aceept Reuest
                    appUsersRef.child(AppModel.shared.USERS[index1!].uID).child("contact").child(String(index2!)).child("requestAction").setValue(3)
                }
            }
            
            
        }
        updateCurrentUserData()
    }
    
    func removeFriendFromUser(_ user : UserModel)
    {
//        var contactArr : [ContactModel] = [ContactModel] ()
        
        let index = AppModel.shared.currentUser.contact.index { (tempContact) -> Bool in
            tempContact.id == user.uID
        }
        
        if index != nil
        {
            AppModel.shared.currentUser.contact.remove(at: index!)
            let index2 = AppModel.shared.currentUser.blockUsers.index { (tempContact) -> Bool in
                tempContact  == user.uID
            }
            
            if index2 != nil
            {
                AppModel.shared.currentUser.blockUsers.remove(at: index2!)
            }
            updateCurrentUserData()
            
            //remove current user from other user contact
            
            let index1 = user.contact.index { (tempContact) -> Bool in
                tempContact.id == AppModel.shared.currentUser.uID
            }
            
            if index1 != nil
            {
                user.contact.remove(at: index1!)
                appUsersRef.child(user.uID).child("contact").child(String(index1!)).removeValue()
                displayToast("Unfriend successfully.")
            }
            
            let index3 = user.blockUsers.index { (tempContact) -> Bool in
                tempContact  == currentUserId()
            }
            
            if index3 != nil
            {
                user.blockUsers.remove(at: index3!)
                appUsersRef.child(user.uID).child("blockUsers").child(String(index3!)).removeValue()
            }
        }
    }
    
    //MARK: STORY
    func uploadStory(story : StoryModel, msg : MessageModel?)
    {
        var chanelID : String = ""
        if msg != nil
        {
            chanelID = createChannel(connectUserId: (msg?.connectUserID)!)
            
        }
        if story.remote_url == ""
        {
            if story.type == 1 //image
            {
                
                if let image = getImage(imageName: story.local_url)
                {
                    AppModel.shared.STORY[story.id] = story
                    self.storyListRef.child(story.id).setValue(story.dictionary())
                    if(msg != nil){
                        self.inboxListRef.child(chanelID).child("lastMessage").setValue(msg!.dictionary())
                    }
                    let imgPath : String = FOLDER.userStory + "/" + story.local_url + ".png"
                    AppModel.shared.UPLOADING_STORY_QUEUE[story.id] = story.id
                    
                    _ = AppDelegate().sharedDelegate().uploadImage(image: image, imagePath: imgPath, completionHandler: { (pic_url) in
                        //print("Image Uploaded")
                        AppModel.shared.UPLOADING_STORY_QUEUE[story.id] = nil
                        if pic_url.count > 0
                        {
                            story.remote_url = pic_url
                            self.storyListRef.child(story.id).setValue(story.dictionary())
                            //displayToast(view: self.window!, message: "Story uploaded successfully.")
                            
                            if chanelID != "" && msg != nil
                            {
                                self.onSendMessage(message: msg!, chanelId: chanelID)
                            }
                            else
                            {
                                if let dictArr : [[String : Any]] = self.sentStoryToFriendBadges[story.id]
                                {
                                    for i in 0..<dictArr.count
                                    {
                                        self.onSendMessage(message: dictArr[i]["msg"] as! MessageModel, chanelId: dictArr[i]["chanelID"] as! String)
                                    }
                                    self.sentStoryToFriendBadges[story.id] = nil
                                }
                            }
                        }
                        else{
                            story.error = "Error"
                            self.storyListRef.child(story.id).setValue(story.dictionary())
                        }
                    }, errorHandler: {(error) in
                        print(error.localizedDescription)
                        AppModel.shared.UPLOADING_STORY_QUEUE[story.id] = nil
                        story.error = error.localizedDescription
                        self.storyListRef.child(story.id).setValue(story.dictionary())
                    })
                }
            }
            else
            {
                story.thumb_local_url = generateThumbImage(VideoName: story.local_url)
                
                AppModel.shared.STORY[story.id] = story
                self.storyListRef.child(story.id).setValue(story.dictionary())
                if(msg != nil){
                    self.inboxListRef.child(chanelID).child("lastMessage").setValue(msg!.dictionary())
                }
                if story.thumb_local_url != ""
                {
                    // Video Thumb Imahe upload
                    let imgPath : String = FOLDER.userStory + "/" + story.thumb_local_url + ".png"
                    _ = AppDelegate().sharedDelegate().uploadImage(image: getImage(imageName: story.thumb_local_url)!, imagePath: imgPath, completionHandler: { (pic_url) in
                        
                        if pic_url.count > 0
                        {
                            story.thumb_remote_url = pic_url
                            self.storyListRef.child(story.id).setValue(story.dictionary())
                            if chanelID != "" && msg != nil && story.remote_url != ""
                            {
                                self.onSendMessage(message: msg!, chanelId: chanelID)
                            }
                            else if story.remote_url != ""
                            {
                                if let dictArr : [[String : Any]] = self.sentStoryToFriendBadges[story.id]
                                {
                                    for i in 0..<dictArr.count
                                    {
                                        self.onSendMessage(message: dictArr[i]["msg"] as! MessageModel, chanelId: dictArr[i]["chanelID"] as! String)
                                    }
                                    self.sentStoryToFriendBadges[story.id] = nil
                                }
                            }
                        }
                        else{
                            story.error = "Error"
                            self.storyListRef.child(story.id).setValue(story.dictionary())
                        }
                    }, errorHandler: {(error) in
                        print(error.localizedDescription)
                        story.error = error.localizedDescription
                        self.storyListRef.child(story.id).setValue(story.dictionary())
                    })
                }
                
                
                // Video Imahe Upload
                AppModel.shared.UPLOADING_STORY_QUEUE[story.id] = story.id
                _ = uploadVideo(videoName: story.local_url, completionHandler: { (video_url) in
                    
                    AppModel.shared.UPLOADING_STORY_QUEUE[story.id] = nil
                    if video_url.count > 0
                    {
                        story.remote_url = video_url
                        self.storyListRef.child(story.id).setValue(story.dictionary())
                        //displayToast(view: self.window!, message: "Story uploaded successfully.")
                        
                        if chanelID != "" && msg != nil && story.thumb_remote_url != ""
                        {
                            self.onSendMessage(message: msg!, chanelId: chanelID)
                        }
                        else if story.thumb_remote_url != ""
                        {
                            if let dictArr : [[String : Any]] = self.sentStoryToFriendBadges[story.id]
                            {
                                for i in 0..<dictArr.count
                                {
                                    self.onSendMessage(message: dictArr[i]["msg"] as! MessageModel, chanelId: dictArr[i]["chanelID"] as! String)
                                }
                                self.sentStoryToFriendBadges[story.id] = nil
                            }
                        }
                    }
                    else{
                        story.error = "Error"
                        self.storyListRef.child(story.id).setValue(story.dictionary())
                    }
                }, errorHandler: {(error) in
                    print(error)
                    AppModel.shared.UPLOADING_STORY_QUEUE[story.id] = nil
                    story.error = error
                    self.storyListRef.child(story.id).setValue(story.dictionary())
                })
            }
        }
        else
        {
            if chanelID != "" && msg != nil
            {
                self.onSendMessage(message: msg!, chanelId: chanelID)
            }
            else
            {
                if let dictArr : [[String : Any]] = self.sentStoryToFriendBadges[story.id]
                {
                    for i in 0..<dictArr.count
                    {
                        self.onSendMessage(message: dictArr[i]["msg"] as! MessageModel, chanelId: dictArr[i]["chanelID"] as! String)
                    }
                    self.sentStoryToFriendBadges[story.id] = nil
                }
            }
        }
    }
    func uploadVideo(videoName: String, completionHandler : @escaping (_ videoURL: String) ->(), errorHandler : @escaping (_ error: String) ->())
    {
        let videoURL = URL(fileURLWithPath: getVideo(videoName: videoName)!)
        
        if let data = try? Data(contentsOf: videoURL) {
            // Get a reference to the storage service using the default Firebase App
            let storage = Storage.storage()
            
            // Create a storage reference from our storage service
            let storageRef = storage.reference()
            
            let videoRef = storageRef.child(FOLDER.userStory + "/" + getCurrentTimeStampValue() + ".mp4")
            let metadata = StorageMetadata()
            metadata.contentType = "video/mp4"
            _ = videoRef.putData(data, metadata: metadata) { metadata, error in
                
                if (error != nil) {
                    print((error?.localizedDescription)!)
                    errorHandler((error?.localizedDescription)!)
                } else {
                    
                    let downloadURL = metadata!.downloadURL()
                    completionHandler(downloadURL!.absoluteString)
                }
            }
        } else {
            errorHandler("Error")
        }
    }
   
   
    func uploadRemainingStory()
    {
        if AppModel.shared.currentUser == nil
        {
            return
        }
        
        for i in 0..<AppModel.shared.currentUser.story.count
        {
            if AppModel.shared.STORY[AppModel.shared.currentUser.story[i]]?.remote_url == ""
            {
                if AppModel.shared.STORY[AppModel.shared.currentUser.story[i]]?.type == 1
                {
                    if getImage(imageName: (AppModel.shared.STORY[AppModel.shared.currentUser.story[i]]?.local_url)!) != nil
                    {
                        uploadStory(story: AppModel.shared.STORY[AppModel.shared.currentUser.story[i]]!, msg: nil)
                    }
                }
                else
                {
                    if getVideo(videoName: (AppModel.shared.STORY[AppModel.shared.currentUser.story[i]]?.local_url)!) != nil
                    {
                        uploadStory(story: AppModel.shared.STORY[AppModel.shared.currentUser.story[i]]!, msg: nil)
                    }
                }
            }
        }
    }
    //MARK: MESSAGE
    func onSendMessage(message : MessageModel, chanelId : String)
    {
        let index2 = AppModel.shared.INBOXLIST.index { (tempInbox) -> Bool in
            tempInbox.id == chanelId
        }
        if chanelId == "" || index2 == nil
        {
            return
        }
        message.status = 2
        messageListRef.child(chanelId).child(message.key).child("status").setValue(message.status)
        
        
        let otherUserBadgeKey : String = getOtherUserBadgeKey(channelID: chanelId)
        var otherUserBadge : Int = 1
        let index = AppModel.shared.INBOXLIST.index { (inbox) -> Bool in
            inbox.id == chanelId
        }
        
        if index != nil
        {
            let inboxList : InboxListModel = AppModel.shared.INBOXLIST[index!]
            if otherUserBadgeKey == "badge1" {
                inboxList.badge1 = inboxList.badge1 + 1
                otherUserBadge = inboxList.badge1
            }
            else
            {
                inboxList.badge2 = inboxList.badge2 + 1
                otherUserBadge = inboxList.badge2
            }
            inboxList.lastMessage = message
        }
        inboxListRef.child(chanelId).child(otherUserBadgeKey).setValue(otherUserBadge)
        
        let index1 = AppModel.shared.USERS.index { (user) -> Bool in
            user.uID == message.connectUserID
        }
        if index1 != nil
        {
            if AppModel.shared.USERS[index1!].last_seen != ""
            {
                sendPush(title: "New Message", body: AppModel.shared.currentUser.name + ((message.text.decodeString != "") ? (" : " + message.text.decodeString) : " has sent story."), user: AppModel.shared.USERS[index1!], type: PUSH_NOTIFICATION.TYPE.CHAT_MESSAGE)
                
                message.status = 3
            }
        }
        inboxListRef.child(chanelId).child("lastMessage").setValue(message.dictionary())
    }
    
    func onGetMessage(message : MessageModel, chanelId : String)
    {
        let myBadgeKey : String = getCurrentUserBadgeKey(chanelId)
        let index = AppModel.shared.INBOXLIST.index { (inbox) -> Bool in
            inbox.id == chanelId
        }
        
        if index != nil
        {
            let inboxList : InboxListModel = AppModel.shared.INBOXLIST[index!]
            if myBadgeKey == "badge1" {
                inboxList.badge1 = 0
            }
            else
            {
                inboxList.badge2 = 0
            }
            //inboxList.lastMessage = message
            inboxListRef.child(chanelId).child(myBadgeKey).setValue(0)
        }
        //inboxListRef.child(chanelId).child("lastMessage").child("badge1")
    }
    func onChannelTap(connectUserId : String)
    {
        let tappedChannelID = createChannel(connectUserId: connectUserId)
        if tappedChannelID != ""
        {
            //reset badges to 0 of badges-loggedInUserId
            
            let badgeRef : DatabaseReference = inboxListRef.child(tappedChannelID).child(getCurrentUserBadgeKey(tappedChannelID))
            badgeRef.setValue(0)
            
            let rootNavigationVc : UINavigationController = self.window?.rootViewController as! UINavigationController
            let vc : ChatViewController = self.storyboard().instantiateViewController(withIdentifier: "ChatViewController") as! ChatViewController
            vc.channelId = tappedChannelID
            rootNavigationVc.pushViewController(vc, animated: true)
        }
    }
    
    func createChannel(connectUserId : String) -> String
    {
        
        if let _ : UserModel = getUserById(uID: connectUserId)
        {
            var strIDArray : [String] = [AppModel.shared.currentUser.uID, connectUserId]
            strIDArray = strIDArray.sorted { $0.localizedCaseInsensitiveCompare($1) == ComparisonResult.orderedAscending }
            let tappedChannelId = strIDArray[0] + "-" + strIDArray[1]
            var isNewChannel : Bool = true
            
            let index = AppModel.shared.INBOXLIST.index { (channel) -> Bool in
                channel.id == tappedChannelId
            }
            
            if index != nil
            {
                isNewChannel = false
            }
            
            if isNewChannel
            {
                let messgaeListModel : InboxListModel = InboxListModel.init(id: tappedChannelId, badge1: 0, badge2: 0, lastMessage: MessageModel.init(dict: [String:Any]()))
                inboxListRef.child(tappedChannelId).setValue(messgaeListModel.dictionary())
            }
            
            
            return tappedChannelId
        }
        return ""
    }
    func getConnectUserDetail(channelId : String) -> UserModel?
    {
        var otherUser : UserModel?
        let arrtemp : [String] = channelId.components(separatedBy: "-")
        if(AppModel.shared.currentUser != nil && arrtemp[0] == AppModel.shared.currentUser.uID){
            otherUser = getUserById(uID: arrtemp[1])
        }
        else{
            otherUser = getUserById(uID: arrtemp[0])
        }
        return otherUser
    }
    func isMyChanel(channelId : String) -> Bool
    {
        let arrtemp : [String] = channelId.components(separatedBy: "-")
        if (arrtemp[0] == AppModel.shared.currentUser.uID) || (arrtemp[1] == AppModel.shared.currentUser.uID)
        {
            return true
        }
        return false
    }
    func getUserById(uID : String) -> UserModel?
    {
        if AppModel.shared.currentUser != nil && uID == AppModel.shared.currentUser.uID
        {
            return AppModel.shared.currentUser
        }
        let index = AppModel.shared.USERS.index { (user) -> Bool in
            user.uID == uID
        }
        
        if index == nil
        {
            return nil
        }
        else
        {
            return AppModel.shared.USERS[index!]
        }
    }
    func getCurrentUserBadgeKey(_ channelID : String) -> String
    {
        let arrTemp : [String] = channelID.components(separatedBy: "-")
        if arrTemp[0] == AppModel.shared.currentUser.uID {
            return "badge1"
        }
        return "badge2"
    }
    func getOtherUserBadgeKey(channelID : String) -> String
    {
        let arrTemp : [String] = channelID.components(separatedBy: "-")
        if arrTemp[0] == AppModel.shared.currentUser.uID {
            return "badge2"
        }
        return "badge1"
    }
    func sendStoryToFriends(_ story : StoryModel, friend : UserModel)
    {
        let messagesRef : DatabaseReference!
        
        let tappedChannelID = createChannel(connectUserId: friend.uID)
        
        if tappedChannelID != ""
        {
            messagesRef = Database.database().reference().child("MESSAGES").child(tappedChannelID)
            
            let newMsgModel: MessageModel = MessageModel.init(msgID: getCurrentTimeStampValue(), key: "", connectUserID: friend.uID, date: getCurrentDateInString(), text: "", storyID : story.id, status:(story.remote_url != "") ? 2 : 1)
            
            
            let newMsgRef : DatabaseReference = messagesRef.childByAutoId()
            newMsgModel.key = newMsgRef.key
            newMsgRef.setValue(newMsgModel.dictionary())
            addMessage(newMsgModel,channelId:tappedChannelID)
            inboxListRef.child(tappedChannelID).child("lastMessage").setValue(newMsgModel.dictionary())
            
            if let _ : [[String : Any]] = sentStoryToFriendBadges[story.id]
            {
                
            }else {
                sentStoryToFriendBadges[story.id] = [[String : Any]] ()
                
            }
            sentStoryToFriendBadges[story.id]?.append(["msg":newMsgModel, "chanelID" : tappedChannelID])
            
        }
    }
    func addMessage(_ newMessage:MessageModel, channelId:String){
        
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
            return
        }
        
        let managedContext = appDelegate.persistentContainer.viewContext
        
        let entity = NSEntityDescription.entity(forEntityName: COREDATA.MESSAGE.TABLE_NAME,
                                                in: managedContext)!
        
        let message = NSManagedObject(entity: entity,
                                      insertInto: managedContext)
        
        message.setValue(channelId, forKeyPath: COREDATA.MESSAGE.CHANNEL_ID)
        message.setValue(newMessage.msgID, forKey: COREDATA.MESSAGE.msgID)
        message.setValue(newMessage.connectUserID, forKey: COREDATA.MESSAGE.connectUserID)
        message.setValue(newMessage.date, forKey: COREDATA.MESSAGE.date)
        message.setValue(newMessage.key, forKey: COREDATA.MESSAGE.key)
        message.setValue(newMessage.status, forKey: COREDATA.MESSAGE.status)
        message.setValue(newMessage.storyID, forKey: COREDATA.MESSAGE.storyID)
        message.setValue(newMessage.text, forKey: COREDATA.MESSAGE.text)
        
        do {
            try managedContext.save()
        } catch let error as NSError {
            print("Could not save. \(error), \(error.userInfo)")
        }
    }
    
    //MARK: Image Uploading
    func uploadUserProfileImage()
    {
        if AppModel.shared.currentUser != nil
        {
            if (AppModel.shared.currentUser.remote_pic_url.count > 0 ){
                
                    UIButton().sd_setBackgroundImage(with: URL(string: AppModel.shared.currentUser.remote_pic_url), for: .normal, completed: { (image, error, SDImageCacheType, url) in
                        if error == nil && image != nil
                        {}
                        else{
                            if(AppModel.shared.currentUser != nil){
                                AppModel.shared.currentUser.remote_pic_url = ""
                                self.updateCurrentUserData()
                                self.uploadUserProfileImage()
                            }
                        }
                    })
                return
            }
            
        }
        if let image = getImage(imageName: AppModel.shared.currentUser.local_pic_url)
        {
            let imageName : String = FOLDER.userPhoto + "/" + AppModel.shared.currentUser.uID + ".png"
            _ = AppDelegate().sharedDelegate().uploadImage(image: image, imagePath: imageName, completionHandler: { (pic_url) in
                //print("Image Uploaded")
                if pic_url.count > 0
                {
                    if AppModel.shared.currentUser != nil{
                        AppModel.shared.currentUser.remote_pic_url = pic_url
                        self.updateCurrentUserData()
                    }
                }
            }, errorHandler: {(error) in
                print(error.localizedDescription)
            })
        }
    }
    
    func uploadImage(image: UIImage, imagePath : String, completionHandler : @escaping (_ imageURL: String) ->(), errorHandler : @escaping (_ error: Error) ->()) {
        
        
        if let data = UIImagePNGRepresentation(image) {
            // Get a reference to the storage service using the default Firebase App
            let storage = Storage.storage()
            
            // Create a storage reference from our storage service
            let storageRef = storage.reference()
            
            let imageRef = storageRef.child(imagePath)
            let metadata = StorageMetadata()
            metadata.contentType = "image/png"
            _ = imageRef.putData(data, metadata: metadata) { metadata, error in
                
                if (error != nil) {
                    print((error?.localizedDescription)!)
                    errorHandler(error!)
                } else {
                    
                    let downloadURL = metadata!.downloadURL()
                    completionHandler(downloadURL!.absoluteString)
                }
            }
        } else {
            completionHandler("")
        }
    }
    
    
    //MARK: login/signup
    func loginWithFacebook()
    {
        let fbLoginManager = FBSDKLoginManager()
        fbLoginManager.logOut()
        if (SLComposeViewController.isAvailable(forServiceType: SLServiceTypeFacebook))
        {
            fbLoginManager.loginBehavior = FBSDKLoginBehavior.systemAccount
        }
        else
        {
            fbLoginManager.loginBehavior = FBSDKLoginBehavior.native;
        }
        fbLoginManager.logIn(withReadPermissions: ["public_profile", "email"], from: window?.rootViewController) { (result, error) in
            if let error = error {
                displayErrorAlertView(title: "Error", message: error.localizedDescription)
                return
            }
            
            guard let accessToken = FBSDKAccessToken.current() else {
                displayErrorAlertView(title: "Error", message: "Failed to get access token")
                return
            }
            
            let credential = FacebookAuthProvider.credential(withAccessToken: accessToken.tokenString)
            
            
            let request : FBSDKGraphRequest = FBSDKGraphRequest(graphPath: "me", parameters: ["fields" : "picture.width(500).height(500), email, id, name, first_name,location"])
            displayLoader()
            let connection : FBSDKGraphRequestConnection = FBSDKGraphRequestConnection()
            connection.add(request, completionHandler: { (connection, result, error) in
                
                if result != nil
                {
                    let dict = result as! [String : AnyObject]
                    //print(dict)
                    
                    let tempUserModel = UserModel.init(uID:  "", email: "", password: "", phoneNo:"", phoneCode:"", phoneId:"", username: "", name: "", location: LocationModel.init(id: "", name: "", image: "", address: "", latitude: 0.0, longitude: 0.0, isOpen: true), height: "", age: USER.AGE, position: USER.POSITION, distance: USER.DISTANCE, local_pic_url: "", remote_pic_url: "", login_type: USER.FB_LOGIN, user_type: USER.REGULAR_USER, courts : [String](), last_seen: "", story: [String] (), contact: [ContactModel](), fcmToken:AppDelegate().sharedDelegate().getFcmToken(), badge : 0, curr_court:"",total_checkIn:0, blockUsers:[String]())
                    
                    if let email = dict["email"]
                    {
                        tempUserModel.email = email as! String
                    }
                    
                    if let userName = dict["first_name"]
                    {
                        tempUserModel.username = userName as! String
                    }
                    
                    if let name = dict["name"]
                    {
                        tempUserModel.name = name as! String
                    }
                    
                    if let picture = dict["picture"] as? [String : Any]
                    {
                        if let data = picture["data"] as? [String : Any]
                        {
                            if let url = data["url"]
                            {
                                tempUserModel.remote_pic_url = url as! String
                            }
                        }
                    }
                    AppModel.shared.currentUser = tempUserModel
                    
                    Auth.auth().signIn(with: credential, completion: { (user, error) in
                        removeLoader()
                        if let error = error {
                            displayErrorAlertView(title: "Error", message: error.localizedDescription)
                            return
                        }
                    })
                }
                else
                {
                    removeLoader()
                    displayErrorAlertView(title: "CheckUp", message: (error?.localizedDescription)!)
                }
            })
            connection.start()
            
            //Curertn user
            // Perform login by calling Firebase APIs
            
        }
    }
    
    func loginWithEmail()
    {
        displayLoader()
        Auth.auth().signIn(withEmail: AppModel.shared.currentUser.email, password: AppModel.shared.currentUser.password) { (user, error) in
            removeLoader()
            if let error = error {
                displayErrorAlertView(title: "Error", message: error.localizedDescription)
                return
            }
        }
    }
    
    func signUpWithEmail()
    {
        displayLoader()
        Auth.auth().createUser(withEmail: AppModel.shared.currentUser.email, password: AppModel.shared.currentUser.password) { (user, error) in
            removeLoader()
            if error != nil
            {
                displayErrorAlertView(title: "Error", message: (error?.localizedDescription)!)
                return
            }
        }
    }
    
    func signUpWithMobile(){
        displayLoader()
        let credential = PhoneAuthProvider.provider().credential(
            withVerificationID: AppModel.shared.currentUser.phoneId,
            verificationCode: AppModel.shared.currentUser.phoneCode)
        Auth.auth().signIn(with: credential) { (user, error) in
            removeLoader()
            if let error = error {
                displayErrorAlertView(title: "Error", message: error.localizedDescription)
                return
            }
            
        }
    }
    
    func forgotPassword(email : String)
    {
        Auth.auth().sendPasswordReset(withEmail: email) { error in
            
            if error != nil
            {
                displayErrorAlertView(title: "Error", message: "Unidentified Email. Please check your email")
            }
            else
            {
                displayErrorAlertView(title: "CheckUp", message: "We send you a recovery email.")
            }
            
        }
    }
    func logout(){
        if AppModel.shared.currentUser != nil
        {
            AppModel.shared.currentUser.last_seen = getCurrentDateInString()
            AppModel.shared.BADGE_COUNT = 0
            AppModel.shared.currentUser.fcmToken = ""
            userFcmToken = ""
            onCheckOutCourt(true)
        }
        self._isGotMyData = false
        self._isSetAllHandler = false
        _isShowMapLoader = false
        _isGetAllUsers = false
        _isGetAllEvents = false
        _isGetAllCourts = false
        _isGetAllStory = false
        _isGetInboxList = false
        AppModel.shared.currentUser = nil
        Preference.sharedInstance.removeUserDefaultValues()
        let firebaseAuth = Auth.auth()
        do {
            try firebaseAuth.signOut()
        } catch let signOutError as NSError {
            print ("Error signing out: %@", signOutError)
        }
        
        let loginManager = FBSDKLoginManager()
        loginManager.logOut() // this is an instance function
    }
    
    //MARK:- BROADCAST ON UPDATE
    func onUpdateBadgeCount()
    {
        if AppModel.shared.currentUser != nil
        {
            getPushBadges()
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: NOTIFICATION.UPDATE_BADGE_COUNT), object: nil)
        }
    }
    func onUpdateCurrentUserLocation()
    {
        if AppModel.shared.currentUser != nil
        {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: NOTIFICATION.UPDATE_CURRENT_USER_LOCATION), object: nil)
        }
    }
    
    func onUpdateAllUser()
    {
        if AppModel.shared.currentUser != nil
        {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: NOTIFICATION.ON_UPDATE_ALL_USER), object: nil)
        }
    }
    
    func onUpdateStories()
    {
        if AppModel.shared.currentUser != nil
        {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: NOTIFICATION.ON_UPDATE_STORIES), object: nil)
        }
    }
    
    func onUpdateEvents()
    {
        if AppModel.shared.currentUser != nil
        {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: NOTIFICATION.ON_UPDATE_EVENTS), object: nil)
        }
    }

    func onUpdateCourts()
    {
        if AppModel.shared.currentUser != nil
        {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: NOTIFICATION.ON_UPDATE_COURTS), object: nil)
        }
    }
   
    func onUpdateInbox()
    {
        if AppModel.shared.currentUser != nil
        {
            NotificationCenter.default.post(name: NSNotification.Name(rawValue: NOTIFICATION.ON_UPDATE_INBOX), object: nil)
        }
    }

    //MARK:- USER FUNC
    func getUserPosition(position : Int) -> String
    {
        switch position {
        case 1:
            return "PG"
        case 2:
            return "SG"
        case 3:
            return "SF"
        case 4:
            return "PF"
        case 5:
            return "C"
        default:
            return ""
        }
    }
    func getFcmToken() -> String
    {
        if userFcmToken == ""
        {
            userFcmToken = Messaging.messaging().fcmToken!
        }
        return userFcmToken
    }
    func setPushBadges()
    {
        AppModel.shared.currentUser.badge = 0
        updateCurrentUserData()
    }
    func getPushBadges()
    {
        if AppModel.shared.currentUser != nil && AppModel.shared.currentUser.uID != nil
        {
            var badgeCount : Int = AppModel.shared.currentUser.badge
            
            for i in 0..<AppModel.shared.INBOXLIST.count
            {
                let inbox = AppModel.shared.INBOXLIST[i]
                if isMyChanel(channelId: inbox.id)
                {
                    let badgeKey = getCurrentUserBadgeKey(inbox.id)
                    if badgeKey == "badge1" && inbox.badge1 > 0
                    {
                        badgeCount = badgeCount + 1
                    }
                    else if badgeKey == "badge2" && inbox.badge2 > 0
                    {
                        badgeCount = badgeCount + 1
                    }
                }
            }
            AppModel.shared.BADGE_COUNT = badgeCount
        }
    }
   
    
    func setUserProfilePopup(selectedUser:UserModel, isStoryDisplay : Bool = true, selectedEvent:EventModel? = nil)
    {
        hideUserProfilePopup()
        userPopup = UINib(nibName: "UserProfileDialogView", bundle: nil).instantiate(withOwner: nil, options: nil)[0] as? UserProfileDialogView
        userPopup?.selectedUser = selectedUser
        userPopup?.selectedEvent = selectedEvent
        userPopup?.isStoryDisplay = isStoryDisplay
        userPopup?.VC = UIApplication.topViewController()
        displaySubViewtoParentView(self.window, subview: userPopup)
    }
    
    func hideUserProfilePopup()
    {
        if userPopup != nil
        {
            userPopup?.removeFromSuperview()
        }
        userPopup = nil
    }
    func reportUser(_ selectedUser:UserModel, subject:String, vc : UIViewController){
        alertController = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
        
        let firstButton = UIAlertAction(title: "Report", style: .default, handler: { (action) -> Void in
            self.sendMailToUser(selectedUser, subject: subject, vc: vc)
        })

        var blockStr:String = "Block"
        if(isBlockUser(selectedUser.uID)){
            blockStr = "Unblock"
        }
        let  secondButton = UIAlertAction(title: blockStr, style: .default, handler: { (action) -> Void in
            self.blockUnblockUser(selectedUser)
        })
        
        let cancelButton = UIAlertAction(title: "Cancel", style: .cancel, handler: { (action) -> Void in

        })

        alertController.addAction(firstButton)
        alertController.addAction(secondButton)
        alertController.addAction(cancelButton)
    
        window?.rootViewController?.present(alertController, animated: true, completion: nil)
    }
    
    func dismissReportVC(){
        if(alertController != nil){
            alertController.dismiss(animated: true, completion: nil)
        }
        alertController = nil
    }
    func blockUnblockUser(_ selectedUser:UserModel){
        let index = AppModel.shared.currentUser.blockUsers.index { (tempUserId) -> Bool in
            tempUserId == selectedUser.uID
        }
        if index != nil{
            AppModel.shared.currentUser.blockUsers.remove(at: index!)
        }
        else{
            AppModel.shared.currentUser.blockUsers.append(selectedUser.uID)
        }
        updateCurrentUserData()
    }
    func isBlockUser(_ selectedUserId:String) -> Bool{
        
        if AppModel.shared.currentUser.blockUsers.contains(selectedUserId){
            return true
        }
        else{
            return false
        }
    }
    func isBlockMe(_ selectedUser:UserModel) -> Bool{
        
        if selectedUser.blockUsers.contains(currentUserId()){
            return true
        }
        else{
            return false
        }
    }
    func sendMailToUser(_ selectedUser:UserModel, subject:String, vc : UIViewController){
        
        if !MFMailComposeViewController.canSendMail() {
            print("Mail services are not available")
            return
        }
        let composeVC = MFMailComposeViewController()
        composeVC.mailComposeDelegate = self
        // Configure the fields of the interface.
        composeVC.setToRecipients([APP.REPORT_EMAIL])
        composeVC.setSubject(subject)
        composeVC.setMessageBody("Reported User : " + selectedUser.username, isHTML: false)
        // Present the view controller modally.
        
        window?.rootViewController?.present(composeVC, animated: true, completion: nil)
    }
    
    func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
        controller.dismiss(animated: true)
    }
    
    //MARK:- Location
    func setLocationManager()
    {
        locationManager = CLLocationManager()
        locationManager.delegate = self
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = CLLocationDistance(USERVALUE.DISTANCE_DIFFERENCE)
        locationManager.pausesLocationUpdatesAutomatically = false
        locationManager.requestAlwaysAuthorization()
        oldLatitude = 0.0
        oldLongitde = 0.0
        
        if CLLocationManager.locationServicesEnabled() {
            //locationManager.startUpdatingHeading()
            locationManager.startUpdatingLocation()
            locationManager.startMonitoringSignificantLocationChanges()
        }
    }
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        let location = locations.last!
        Preference.sharedInstance.setUserLocation(latitude: Float(location.coordinate.latitude), longitude: Float(location.coordinate.longitude))
        //manager.stopUpdatingLocation()
        //print(location)
        if AppModel.shared.currentUser != nil && AppModel.shared.currentUser.uID.count > 0 {
                if oldLatitude != 0.0 && oldLongitde != 0.0 && oldLatitude != Float(location.coordinate.latitude) && oldLongitde != Float(location.coordinate.longitude)
                {
                    let oldCoordinate = CLLocation(latitude: CLLocationDegrees(oldLatitude), longitude: CLLocationDegrees(oldLongitde))
                    let newCoordinate = CLLocation(latitude: location.coordinate.latitude, longitude: location.coordinate.longitude)
                    
                    let distanceInMeters = oldCoordinate.distance(from: newCoordinate) // result is in meters
                    //displayToast(view: window!, message: "Distance : " + String(distanceInMeters))
                    
                    
                    //checkout from court
                    if AppModel.shared.currentUser.curr_court != "" && AppModel.shared.CURRENT_CHECKIN_COURT != nil
                    {
                        let lastCourtCoordinate = CLLocation(latitude: CLLocationDegrees(AppModel.shared.CURRENT_CHECKIN_COURT.location.latitude), longitude: CLLocationDegrees(AppModel.shared.CURRENT_CHECKIN_COURT.location.longitude))
                        
                        let distanceInMeters = lastCourtCoordinate.distance(from: newCoordinate) // result is in meters
                        
                        if distanceInMeters > CLLocationDistance(USERVALUE.LEAVING_DIFFERENCE)
                        {
                            onCheckOutCourt()
                        }
                    }
                    if distanceInMeters > CLLocationDistance(USERVALUE.DISTANCE_DIFFERENCE) //meter
                    {
                        //print("Location change")
                        getCourtNearByMe()
                    }
                    
                }
                else if (oldLongitde == 0.0 && oldLatitude == 0.0)
                {
                    getCourtNearByMe()
                }
          
            oldLatitude = Float(location.coordinate.latitude)
            oldLongitde = Float(location.coordinate.longitude)
            
            onUpdateCurrentUserLocation()
        }
    }
   
    
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print(error.localizedDescription)
    }
    func getCourtNearByMe()
    {
        //print(AppModel.shared.currentUser.distance)
        //print(Float(AppModel.shared.currentUser.distance) * Float(1609.34))
        let distance:Int = AppModel.shared.currentUser == nil ? USER.DISTANCE : AppModel.shared.currentUser.distance
        let urlString = String(format: API.NEAR_COURT_GYM, Preference.sharedInstance.getUserLatitude(), Preference.sharedInstance.getUserLongitude(), Float(distance) * Float(1609.34))
        print(urlString)
        Alamofire.request(urlString, method: .post, parameters: ["" : ""],encoding: JSONEncoding.default, headers: nil).responseJSON {
            response in
            switch response.result {
            case .success:
                var locations:[LocationModel] = [LocationModel] ()
                if let JSON = response.result.value as? [String:Any]
                {
                    if let response = JSON["results"] as? [[String:Any]]
                    {
                        
                        for item in response
                        {
                            //print(item)
                            let id = item["id"] as! String
                            let name = item["name"] as! String
                            let address = item["vicinity"] as! String
                            let latitude = ((item["geometry"] as! [String:Any])["location"] as! [String:Any])["lat"] as! Float
                            let longitude = ((item["geometry"] as! [String:Any])["location"] as! [String:Any])["lng"] as! Float
                            
                            var img_url = ""
                            if item["photos"] != nil
                            {
                                let photoArr : [AnyObject] = (item["photos"] as? [AnyObject])!
                                let photoDict = photoArr[0] as! [String : AnyObject]
                                img_url = String(format: API.GOOGLE_IMAGE, photoDict["photo_reference"] as! String)
                            }
                            
                            var isOpen:Bool = true
                            if item["opening_hours"] != nil
                            {
                                isOpen = (item["opening_hours"] as! [String : AnyObject])["open_now"] as! Bool
                            }
                            
                            locations.append(LocationModel.init(id: id, name: name, image: img_url, address: address, latitude: latitude, longitude: longitude, isOpen: isOpen))
                        }
                        AppModel.shared.PRELOCATED_COURTS = locations
                    }
                    if let error = JSON["error_message"] as? String{
                        displayToast(error)
                    }
                    self.onUpdateCourts()
                }
                break
            case .failure(let error):
                
                print(error)
                break
            }
        }
    }
    
    //MARK:- OTHER FUNC
    func setUserProfileImage(_ uID : String, button : UIButton)
    {
        if let user : UserModel = AppDelegate().sharedDelegate().getUserById(uID: uID)
        {
            if let image = getImage(imageName: user.local_pic_url)
            {
                button.setBackgroundImage(image, for: UIControlState.normal)
            }
            else
            {
                if(user.remote_pic_url == ""){
                    button.setBackgroundImage(UIImage(named:"user_placeholder"), for: .normal)
                }
                else{
                    button.sd_setBackgroundImage(with: URL(string:user.remote_pic_url), for: .normal, placeholderImage: UIImage(named:"user_placeholder"))
                }
            }
        }
        else{
            button.setBackgroundImage(UIImage(named:"user_placeholder"), for: .normal)
        }
    }
    func setCourtImage(_ url : String, button : UIButton)
    {
        if(url == ""){
            button.setBackgroundImage(UIImage(named:"court_placeholder"), for: .normal)
        }
        else{
            button.sd_setBackgroundImage(with: URL(string:url), for: .normal, placeholderImage: UIImage(named:"court_placeholder"))
        }
        
    }
    func showPopUpAlertViewWithOneButton(title:String, msg:String)
    {
        alert = self.storyboard().instantiateViewController(withIdentifier: VIEW.ALERT) as! customAlertView
        alert.delegate = self
        self.window?.addSubview(alert.view)
        displaySubViewtoParentView(self.window, subview: alert.view)
        
        alert.view.transform = CGAffineTransform(scaleX: 0.4, y: 0.4)
        UIView.animate(withDuration: 0.35, delay: 0.0, usingSpringWithDamping: 0.55, initialSpringVelocity: 1.0, options: [], animations: {() -> Void in
            self.alert.view.transform = CGAffineTransform.identity
        }, completion: {(_ finished: Bool) -> Void in
        })
        
        alert.alertTitle(title: title, alertMessage: msg, cancelBtnTitle: NSLocalizedString("ok_button", comment: ""), otherBtnTitle: "")
    }
    
    func selectDidSingleCancel() {
        
        UIView.animate(withDuration: 0.25, animations: {() -> Void in
            self.alert.view.transform = CGAffineTransform(scaleX: 0.65, y: 0.65)
            self.alert.view.alpha = 0.0
        }, completion: {(_ finished: Bool) -> Void in
            self.alert.view.removeFromSuperview()
        })
    }
    
    func selectDidCancel()
    {
        
    }
    func displayActivityLoader()
    {
        removeActivityLoader()
        activityLoader = NVActivityIndicatorView(frame: CGRect(x: ((window?.frame.size.width)!-50)/2, y: ((window?.frame.size.height)!-50)/2, width: 50, height: 50))
        activityLoader.type = .ballSpinFadeLoader
        activityLoader.color = colorFromHex(hex: COLOR.APP_COLOR)
        window?.addSubview(activityLoader)
        activityLoader.startAnimating()
    }
    
    func removeActivityLoader()
    {
        if activityLoader == nil
        {
            return
        }
        activityLoader.stopAnimating()
        activityLoader.removeFromSuperview()
        activityLoader = nil
    }
    
    
    //MARK:- Navigation
    func navigateToSignInSignUp()
    {
        let navigationVC = self.storyboard().instantiateViewController(withIdentifier: "SignInSignUpVCNav") as! UINavigationController
        UIApplication.shared.keyWindow?.rootViewController = navigationVC
    }
    
    func navigateToDashBoard()
    {
        let navigationVC = self.storyboard().instantiateViewController(withIdentifier: "DashboardVCNav") as! UINavigationController
        UIApplication.shared.keyWindow?.rootViewController = navigationVC
    }
    
    func navigateToDisplayStory(_ user : UserModel)
    {
        let rootNavigationVc : UINavigationController = self.window?.rootViewController as! UINavigationController
        let vc : DisplayStoryVC = self.storyboard().instantiateViewController(withIdentifier: "DisplayStoryVC") as! DisplayStoryVC
        vc.arrUser = [user]
        vc.mainIndex = 0
        rootNavigationVc.pushViewController(vc, animated: true)
    }
    
    func navigateToEditProfile()
    {
        let rootNavigationVc : UINavigationController = self.window?.rootViewController as! UINavigationController
        let vc : EditProfileVC = self.storyboard().instantiateViewController(withIdentifier: "EditProfileVC") as! EditProfileVC
        rootNavigationVc.pushViewController(vc, animated: true)
    }
    
    //MARK:- Notification
    func registerPushNotification(_ application: UIApplication)
    {
        //setup Messaging
        Messaging.messaging().delegate = self
        
        //--------------setup FCM for push notification------------------//
        if #available(iOS 10.0, *) {
            // For iOS 10 display notification (sent via APNS)
            UNUserNotificationCenter.current().delegate = self
            
            let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
            UNUserNotificationCenter.current().requestAuthorization(
                options: authOptions,
                completionHandler: {_, _ in })
        } else {
            let settings: UIUserNotificationSettings =
                UIUserNotificationSettings(types: [.alert, .badge, .sound], categories: nil)
            application.registerUserNotificationSettings(settings)
        }
        
        application.registerForRemoteNotifications()
        //--------------setup FCM for push notification------------------//
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any]) {
        //print(userInfo)
        application.applicationIconBadgeNumber = 0
        if let apsDict : [String : Any] = userInfo["aps"] as? [String:Any]
        {
            application.applicationIconBadgeNumber = apsDict["badge"] as! Int
        }
        if UIApplication.shared.applicationState == .active
        {
            let uploadContent = UNMutableNotificationContent()
            
            if let apsDict : [String : Any] = userInfo["aps"] as? [String:Any]
            {
                if let alert : [String:Any] = apsDict["alert"] as? [String : Any]
                {
                    uploadContent.title = alert["title"] as? String ?? ""
                    uploadContent.body = alert["body"] as? String ?? ""
                }
            }
            uploadContent.userInfo = userInfo
            
            let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
            let uploadRequestIdentifier = "localPushIdentifier"
            let uploadRequest = UNNotificationRequest(identifier: uploadRequestIdentifier, content: uploadContent, trigger: trigger)
            UNUserNotificationCenter.current().add(uploadRequest, withCompletionHandler: nil)
        }
    
    }
    
    func application(_ application: UIApplication, didReceiveRemoteNotification userInfo: [AnyHashable: Any],
                     fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void) {
        print(userInfo)
        if Auth.auth().canHandleNotification(userInfo) {
            completionHandler(.noData)
            return
        }
        application.applicationIconBadgeNumber = 0
        if let apsDict : [String : Any] = userInfo["aps"] as? [String:Any]
        {
            application.applicationIconBadgeNumber = apsDict["badge"] as! Int
        }
        
        // This notification is not auth related, developer should handle it.
        
        completionHandler(UIBackgroundFetchResult.newData)
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("Unable to register for remote notifications: \(error.localizedDescription)")
    }
    
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        print("APNs token retrieved: \(deviceToken)")
        Messaging.messaging().apnsToken = deviceToken

    }
    
    func sendPush(title:String, body:String, user:UserModel, type : String, otherId: String = "")
    {
        /*
         1. Friend request
         2. today's my event
         3. get message with text or story
         4. My friend get alert when I checekdIn
         */
        
        let url  = NSURL(string: "https://fcm.googleapis.com/fcm/send")
        
        let request = NSMutableURLRequest(url: url! as URL)
        
        request.setValue("application/json", forHTTPHeaderField:"Content-Type")
        request.setValue("key=AAAAdd48AHg:APA91bHkfU3tRDL1ehtFsDGD2r59xdh17z3s4sEoRHucHP2f0z8lIQ2neRMisf0Ijsfi65OcWhagv4o_Mi9wXw5zxtTknNllwY3p9Q-Q6IX-75hzIFbuPEwuATzE-NtzerCjCALTvy7W", forHTTPHeaderField: "Authorization")
        
        request.httpMethod = "POST"
        
        //badge
        var userBadge : Int = user.badge
        if (type == PUSH_NOTIFICATION.TYPE.EVENT_INVITE) || (type == PUSH_NOTIFICATION.TYPE.FRIEND_REQUEST) || (type == PUSH_NOTIFICATION.TYPE.TODAY_EVENT)
        {
            userBadge = userBadge + 1
            self.appUsersRef.child(user.uID).child("badge").setValue(userBadge)
        }
        
        let sessionConfig = URLSessionConfiguration.default
        
        let token = user.fcmToken
                                                        
        var extraOtherId:String = otherId
        if (extraOtherId == ""){
            extraOtherId = AppModel.shared.currentUser.uID
        }
        let json = ["to":token!,
                    "priority":"high",
                    "content_available":true,
                    "notification":["sound" : "default", "badge" : String(userBadge), "body":body,"title":title,"body_loc_key":type, "body_loc_args":[extraOtherId]]] as [String : Any]
        
        do {
            
            let jsonData = try JSONSerialization.data(withJSONObject: json, options: JSONSerialization.WritingOptions.prettyPrinted)
            
            request.httpBody = jsonData
            
            let urlSession = URLSession(configuration: sessionConfig, delegate: self, delegateQueue: OperationQueue.main)
            let datatask = urlSession.dataTask(with: request as URLRequest) { (data, response, error) in
                if data != nil
                {
//                    let strData = NSString(data: data!, encoding: String.Encoding.utf8.rawValue)
                    //print("Body: \(String(describing: strData))")
                    //print(response ?? "",data ?? "")
                    print(error ?? "")
                    
                }
                
            }
            
            datatask.resume()
            
        } catch let error as NSError {
            print(error)
        }
    }
    @available(iOS 10.0, *)
    func showLocalPush(title : String, subTitle : String, user : UserModel)
    {
        
        let index = AppModel.shared.USERS.index(where: { (tempUser) -> Bool in
            tempUser.uID == user.uID
        })
        
        if index != nil
        {
            
            let uploadContent = UNMutableNotificationContent()
            uploadContent.title = title
            uploadContent.body = subTitle
            uploadContent.userInfo = ["id" : user.uID]
            uploadContent.categoryIdentifier = "CHAT"
            
            let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
            
            let uploadRequestIdentifier = "myChatIdentifier"
            let uploadRequest = UNNotificationRequest(identifier: uploadRequestIdentifier, content: uploadContent, trigger: trigger)
            UNUserNotificationCenter.current().add(uploadRequest, withCompletionHandler: nil)
        }
        
    }
    
    @IBAction func clickToLocalPush(_ sender: AnyObject)
    {
        self.localView.removeFromSuperview()
        self.localView = nil
        if AppModel.shared.USERS[sender.tag].uID != nil && AppModel.shared.USERS[sender.tag].uID != ""
        {
            onChannelTap(connectUserId: AppModel.shared.USERS[sender.tag].uID)
        }
    }
    
    // MARK: - Core Data stack
    lazy var persistentContainer: NSPersistentContainer = {
        /*
         The persistent container for the application. This implementation
         creates and returns a container, having loaded the store for the
         application to it. This property is optional since there are legitimate
         error conditions that could cause the creation of the store to fail.
         */
        let container = NSPersistentContainer(name: "CheckUpModel")
        container.loadPersistentStores(completionHandler: { (storeDescription, error) in
            if let error = error as NSError? {
                // Replace this implementation with code to handle the error appropriately.
                // fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
                
                /*
                 Typical reasons for an error here include:
                 * The parent directory does not exist, cannot be created, or disallows writing.
                 * The persistent store is not accessible, due to permissions or data protection when the device is locked.
                 * The device is out of space.
                 * The store could not be migrated to the current model version.
                 Check the error message to determine what the actual problem was.
                 */
                fatalError("Unresolved error \(error), \(error.userInfo)")
            }
        })
        return container
    }()
    
    
    func saveContext () {
        let context = persistentContainer.viewContext
        if context.hasChanges {
            do {
                try context.save()
            } catch {
                // Replace this implementation with code to handle the error appropriately.
                // fatalError() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development.
                let nserror = error as NSError
                fatalError("Unresolved error \(nserror), \(nserror.userInfo)")
            }
        }
    }
    
    func sortAllCoreData()
    {
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
            return
        }
        
        var messagesArr: [NSManagedObject] = [NSManagedObject] ()
        let managedContext = appDelegate.persistentContainer.viewContext
        
        let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: COREDATA.MESSAGE.TABLE_NAME)
        
        do {
            messagesArr = try managedContext.fetch(fetchRequest)
            deleteAllMessageFromCoreData("")
            
//            if messagesArr.count > 1
//            {
//                messagesArr.sort {
//                    let elapsed0 = $0.value(forKey: "msgID") as! String
//                    let elapsed1 = $1.value(forKey: "msgID") as! String
//                    return elapsed0 < elapsed1
//                }
//            }
            
            let entity = NSEntityDescription.entity(forEntityName: COREDATA.MESSAGE.TABLE_NAME,
                                                    in: managedContext)!
            
            for tempMsg in messagesArr
            {
                var message = NSManagedObject(entity: entity, insertInto: managedContext)
                message = tempMsg
                do {
                    try managedContext.save()
                    
                } catch let error as NSError {
                    print("Could not save. \(error), \(error.userInfo)")
                }
            }
            
            
        } catch let error as NSError {
            print("Could not fetch. \(error), \(error.userInfo)")
        }
    }
    
    func deleteAllMessageFromCoreData(_ channelId : String)
    {
        guard let appDelegate = UIApplication.shared.delegate as? AppDelegate else {
            return
        }
        
        let managedContext = appDelegate.persistentContainer.viewContext
        
        let fetchRequest = NSFetchRequest<NSManagedObject>(entityName: COREDATA.MESSAGE.TABLE_NAME)
        do {
            let messagesArr: [NSManagedObject] = try managedContext.fetch(fetchRequest)
            
            for msg in messagesArr
            {
                if channelId == ""
                {
                    managedContext.delete(msg)
                }
                else if msg.value(forKey: COREDATA.MESSAGE.CHANNEL_ID) as! String == channelId
                {
                    managedContext.delete(msg)
                }
            }
        } catch let error as NSError {
            print("Could not fetch. \(error), \(error.userInfo)")
        }
    }

    //MARK: Application delegate
    func application(_ app: UIApplication, open url: URL, options: [UIApplicationOpenURLOptionsKey : Any] = [:]) -> Bool
    {
        return FBSDKApplicationDelegate.sharedInstance().application(app, open: url, options: options)
    }
    
    func applicationWillResignActive(_ application: UIApplication) {
        // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
        // Use this method to pause ongoing tasks, disable timers, and invalidate graphics rendering callbacks. Games should use this method to pause the game.
    }
    
    func applicationDidEnterBackground(_ application: UIApplication) {
        // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later.
        // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
        if(locationManager != nil){
            locationManager.startMonitoringSignificantLocationChanges()
        }
        updateLastSeen(isOnline: false)
    }
    
    func applicationWillEnterForeground(_ application: UIApplication) {
        // Called as part of the transition from the background to the active state; here you can undo many of the changes made on entering the background.
        
    }
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
        updateLastSeen(isOnline: true)
        application.applicationIconBadgeNumber = 0
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
        updateLastSeen(isOnline: false)
        self.saveContext()
    }
    /*
    
    func removePastStory()
    {
        
         var removeStoryArr : [String] = [String] ()
         
         for story in AppModel.shared.STORY
         {
         if isPastStory(story: AppModel.shared.STORY[story.key]!)
         {
         removeStoryArr.append(story.key)
         AppModel.shared.STORY[story.key] = nil
         storyListRef.child(story.key).removeValue()
         }
         }
         
         for i in 0..<removeStoryArr.count{
         
         //remove from current user story
         let index = AppModel.shared.currentUser.story.index(where: { (temp) -> Bool in
         temp == removeStoryArr[i]
         })
         if(index != nil){
         AppModel.shared.currentUser.story.remove(at: index!)
         updateCurrentUserData()
         }
         
         //remove from app user
         for j in 0..<AppModel.shared.USERS.count{
         let index1 = AppModel.shared.USERS[j].story.index(where: { (temp) -> Bool in
         temp == removeStoryArr[i]
         })
         if(index1 != nil){
         AppModel.shared.USERS[j].story.remove(at: index1!)
         appUsersRef.child(AppModel.shared.USERS[j].uID).child("story").setValue(AppModel.shared.USERS[j].story)
         }
         }
         
         //remove from court
         for j in 0..<AppModel.shared.COURTS.count
         {
         let index2 = AppModel.shared.COURTS[j].story.index(where: { (temp) -> Bool in
         temp == removeStoryArr[i]
         })
         if(index2 != nil){
         AppModel.shared.COURTS[j].story.remove(at: index2!)
         courtRef.child(AppModel.shared.COURTS[j].location.id).child("story").setValue(AppModel.shared.COURTS[j].story)
         }
         }
         }
 
    }
    func removeCurrUserPastStory()
    {
        if AppModel.shared.STORY.count == 0
        {
            return
        }
        var storyArr : [String] = [String] ()
        var removeStoryArr : [String] = [String] ()
        for i in 0..<AppModel.shared.currentUser.story.count
        {
            if let story : StoryModel = AppModel.shared.STORY[AppModel.shared.currentUser.story[i]]
            {
                if isPastStory(story: story)
                {
                    removeStoryArr.append(story.id)
                }
                else
                {
                    storyArr.append(story.id)
                }
            }
        }
        if(storyArr.count == 0){
            storyArr = AppModel.shared.currentUser.story
        }
        AppModel.shared.currentUser.story = storyArr
        updateCurrentUserData()
        
        for i in 0..<removeStoryArr.count
        {
            storyListRef.child(removeStoryArr[i]).removeValue()
            updateCurrentUserData()
        }
        
        for i in 0..<AppModel.shared.COURTS.count
        {
            let court : CourtModel = AppModel.shared.COURTS[i]
            storyArr = [String] ()
            removeStoryArr = [String] ()
            for j in 0..<court.story.count
            {
                if let story : StoryModel = AppModel.shared.STORY[court.story[j]]
                {
                    if story.uID == AppModel.shared.currentUser.uID
                    {
                        if isPastStory(story: story)
                        {
                            removeStoryArr.append(story.id)
                        }
                        else
                        {
                            storyArr.append(story.id)
                        }
                    }
                    else
                    {
                        storyArr.append(story.id)
                    }
                }
            }
            if(storyArr.count == 0){
                storyArr = AppModel.shared.COURTS[i].story
            }
            AppModel.shared.COURTS[i].story = storyArr
            courtRef.child(court.location.id).child("story").setValue(storyArr)
            
            for i in 0..<removeStoryArr.count
            {
                storyListRef.child(removeStoryArr[i]).removeValue()
            }
        }
        
    }
    func removePastCreatedCourt()
    {
        
         for i in 0..<AppModel.shared.COURTS.count
         {
         let court = AppModel.shared.COURTS[i]
         
         if court.type == 2 && isPastCourt(strDate: court.date)
         {
         removeCreatedCourt(court,index:i)
         }
         }
 
    }
    func removePastEvent()
    {
         var removeEventArr : [String] = [String] ()
         var eventArr :[EventModel] = [EventModel] ()
         let len:Int = AppModel.shared.EVENTS.count
         for i in 0..<len{
         if isPastEvent(event: AppModel.shared.EVENTS[i])
         {
         removeEventArr.append(AppModel.shared.EVENTS[i].id)
         eventsRef.child(AppModel.shared.EVENTS[i].id).removeValue()
         }
         else{
         eventArr.append(AppModel.shared.EVENTS[i])
         }
         }
         AppModel.shared.EVENTS = eventArr
         
         for i in 0..<removeEventArr.count{
         
         //remove from court
         for j in 0..<AppModel.shared.COURTS.count
         {
         let index2 = AppModel.shared.COURTS[j].activity.index(where: { (temp) -> Bool in
         temp == removeEventArr[i]
         })
         if(index2 != nil){
         AppModel.shared.COURTS[j].activity.remove(at: index2!)
         courtRef.child(AppModel.shared.COURTS[j].location.id).child("activity").setValue(AppModel.shared.COURTS[j].activity)
         }
         }
         }
        
    }
    
    func removeCurrUserPastEvent()
    {
        
        if AppModel.shared.EVENTS.count == 0
        {
            return
        }
        var arrRemoveId : [String] = [String] ()
        var newEvent : [EventModel] = [EventModel]()
        for i in 0..<AppModel.shared.EVENTS.count
        {
            if AppModel.shared.EVENTS[i].uID == AppModel.shared.currentUser.uID
            {
                if isPastEvent(event: AppModel.shared.EVENTS[i])
                {
                    arrRemoveId.append(AppModel.shared.EVENTS[i].id)
                    continue
                }
            }
            newEvent.append(AppModel.shared.EVENTS[i])
            
        }
        AppModel.shared.EVENTS = newEvent
        
        for j in 0..<arrRemoveId.count
        {
            eventsRef.child(arrRemoveId[j]).removeValue()
        }
        
        
        for i in 0..<AppModel.shared.COURTS.count
        {
            if arrRemoveId.count == 0
            {
                break
            }
            var isUpdated : Bool = false
            var newArr : [String] = [String] ()
            for j in 0..<arrRemoveId.count
            {
                let index = AppModel.shared.COURTS[i].activity.index(where: { (tempActivity) -> Bool in
                    tempActivity == arrRemoveId[j]
                })
                
                if index != nil
                {
                    AppModel.shared.COURTS[i].activity.remove(at: index!)
                    isUpdated = true
                }
                else
                {
                    newArr.append(arrRemoveId[j])
                }
            }
            arrRemoveId = newArr
            if isUpdated == true
            {
                courtRef.child(AppModel.shared.COURTS[i].location.id).child("activity").setValue(AppModel.shared.COURTS[i].activity)
            }
            
        }
        
    }
 */
    
}


@available(iOS 10, *)
extension AppDelegate : UNUserNotificationCenterDelegate {
    
    // Receive displayed notifications for iOS 10 devices.
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                willPresent notification: UNNotification,
                                withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        _ = notification.request.content.userInfo
        //        print(userInfo)
        
        // Change this to your preferred presentation option
        //completionHandler([])
        completionHandler([.alert, .badge, .sound])
        
    }
    
    func userNotificationCenter(_ center: UNUserNotificationCenter,
                                didReceive response: UNNotificationResponse,
                                withCompletionHandler completionHandler: @escaping () -> Void) {
        let userInfo = response.notification.request.content.userInfo
        
        if response.notification.request.content.categoryIdentifier == "CHAT"
        {
            onChannelTap(connectUserId: userInfo["id"] as! String)
        }
        else if let apsDict : [String : Any] = userInfo["aps"] as? [String:Any]
        {
            if let alert : [String:Any] = apsDict["alert"] as? [String : Any]
            {
                if UIApplication.shared.applicationState == .inactive
                {
                    _ = Timer.scheduledTimer(timeInterval: 2.0, target: self, selector: #selector(delayForNotification(tempTimer:)), userInfo: userInfo, repeats: false)
                }
                else
                {
                    notificationHandler(alert: alert)
                }
                
            }
        }
        
        
        completionHandler()
    }
    
    func delayForNotification(tempTimer:Timer)
    {
        notificationHandler(alert: tempTimer.userInfo as! [String : Any])
    }
    
    //Redirect to screen
    func notificationHandler(alert : [String:Any] )
    {
        let currentVC : UIViewController? = UIApplication.topViewController()
        if let type = alert["loc-key"] as? String
        {
            let rootNavigationVc : UINavigationController = self.window?.rootViewController as! UINavigationController
            if type == PUSH_NOTIFICATION.TYPE.CHAT_MESSAGE
            {
                if alert["loc-args"] != nil{
                    if currentVC != nil && currentVC is ChatViewController && (currentVC as! ChatViewController).channelId ==  (alert["loc-args"] as! [String]).first!
                    {}
                    else{
                        onChannelTap(connectUserId: (alert["loc-args"] as! [String]).first!)
                    }
                }
            }
            else //if (type == PUSH_NOTIFICATION.TYPE.FRIEND_REQUEST) || (type == PUSH_NOTIFICATION.TYPE.TODAY_EVENT) || (type == PUSH_NOTIFICATION.TYPE.EVENT_INVITE)
            {
                if(type == PUSH_NOTIFICATION.TYPE.CHECKED_IN || type == PUSH_NOTIFICATION.TYPE.COURT_COMMENT || type == PUSH_NOTIFICATION.TYPE.WANT_TO_CHECK_IN){
                    if(alert["loc-args"] != nil){
                        let extraOtherId = (alert["loc-args"] as! [String]).first!
                        let index = AppModel.shared.COURTS.index(where: { (tempCourt) -> Bool in
                            tempCourt.location.id == extraOtherId
                        })
                        if(index != nil){
                            if currentVC != nil && currentVC is CourtDetailVC && (currentVC as! CourtDetailVC).currCourtModel.location.id ==  AppModel.shared.COURTS[index!].location.id
                            {}
                            else{
                                let vc : CourtDetailVC = self.storyboard().instantiateViewController(withIdentifier: "CourtDetailVC") as! CourtDetailVC
                                vc.currCourtModel = AppModel.shared.COURTS[index!]
                                rootNavigationVc.pushViewController(vc, animated: true)
                            }
                        }
                    }
                }
                else if(type == PUSH_NOTIFICATION.TYPE.EVENT_INVITE || type == PUSH_NOTIFICATION.TYPE.EVENT_COMMENT || type == PUSH_NOTIFICATION.TYPE.EVENT_JOIN || type == PUSH_NOTIFICATION.TYPE.EVENT_DECLINED || type == PUSH_NOTIFICATION.TYPE.EVENT_CANCELLED){
                    if(alert["loc-args"] != nil){
                        let extraOtherId = (alert["loc-args"] as! [String]).first!
                        let index = AppModel.shared.EVENTS.index(where: { (tempEvent) -> Bool in
                            tempEvent.id == extraOtherId
                        })
                        if(index != nil){
                            if currentVC != nil && currentVC is GameDetailVC && (currentVC as! GameDetailVC).eventModel.id ==  AppModel.shared.EVENTS[index!].id
                            {
                                
                            }
                            else{
                                let vc : GameDetailVC = self.storyboard().instantiateViewController(withIdentifier: "GameDetailVC") as! GameDetailVC
                                vc.eventModel = AppModel.shared.EVENTS[index!]
                                rootNavigationVc.pushViewController(vc, animated: true)
                            }
                        }
                    }
                }
                else{
                    if (currentVC is MessageNotificationVC) == false
                    {
                        let vc : MessageNotificationVC = self.storyboard().instantiateViewController(withIdentifier: "MessageNotificationVC") as! MessageNotificationVC
                        vc.isMessageDisplay = false
                        rootNavigationVc.pushViewController(vc, animated: true)
                    }
                }
            }
            
        }
    }
    
}


extension AppDelegate : MessagingDelegate {
    func messaging(_ messaging: Messaging, didRefreshRegistrationToken fcmToken: String) {
        //print("Firebase registration token: \(fcmToken)")
        userFcmToken = fcmToken
    }
    
    func messaging(_ messaging: Messaging, didReceive remoteMessage: MessagingRemoteMessage) {
        //print("Received data message: \(remoteMessage.appData)")
    }
}


extension UIApplication {
    class func topViewController(base: UIViewController? = (UIApplication.shared.delegate as! AppDelegate).window?.rootViewController) -> UIViewController? {
        if let nav = base as? UINavigationController {
            return topViewController(base: nav.visibleViewController)
        }
        if let tab = base as? UITabBarController {
            if let selected = tab.selectedViewController {
                return topViewController(base: selected)
            }
        }
        if let presented = base?.presentedViewController {
            return topViewController(base: presented)
        }
        return base
    }
}

