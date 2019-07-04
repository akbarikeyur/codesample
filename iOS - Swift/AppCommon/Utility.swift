//
//  Utility.swift
//
//
//  Created by Keyur on 22/12/17.
//  Copyright © 2017 Keyur. All rights reserved.
//

import UIKit

//MARK:- Date func
func getDateStringFromDate(_ date:Date, format:String) -> String{
    var dateFormmat = DateFormatter()
    dateFormmat.dateFormat = format
    return dateFormmat.string(from: date)
}
func getDateFromString(_ str:String, currFormat:String) -> Date?{
    var dateFormmat = DateFormatter()
    dateFormmat.dateFormat = currFormat
    if let date = dateFormmat.date(from: str){
        return date
    }
    else{
        return nil
    }
}
func getTimeIntFromString(_ str:String) -> Int{
    var arr:[String] = str.components(separatedBy: ":")
    return (Int(arr[0])! * 100) + Int(arr[1])!
}
func getTimeStringFromInt(_ time:Int) -> String{
    let h:Int = Int(time / 100)
    let m:Int = time - (h * 100)
    var hStr:String = String(h)
    var mStr:String = String(m)
    if(hStr.count == 1){
        hStr = "0" + hStr
    }
    if(mStr.count == 1){
        mStr = "0" + mStr
    }
    return hStr+":"+mStr
}

func getDateStringFromString(_ str:String, currFormat:String, format:String) -> String{
    var dateFormmat = DateFormatter()
    if(currFormat != "")
    {
        dateFormmat.dateFormat = currFormat
    }
    if let date = dateFormmat.date(from: str){
        dateFormmat.dateFormat = format
        //dateFormmat.timeZone = TimeZone(identifier: "UTC")
        return dateFormmat.string(from: date)
    }
    else{
        return ""
    }
}
func getDateStringFromStringWithTimeZone(_ str:String, currFormat:String, format:String) -> String{
    var dateFormmat = DateFormatter()
    dateFormmat.timeZone = TimeZone(identifier: CONSTANT.TIME_ZONE)
    
    if(currFormat != "")
    {
        dateFormmat.dateFormat = currFormat
    }
    if let date = dateFormmat.date(from: str){
        dateFormmat.dateFormat = format
        dateFormmat.timeZone = TimeZone.ReferenceType.default
        return dateFormmat.string(from: date)
    }
    else{
        return ""
    }
}

//MARK:- User function
func saveUser()
{
    if let user:UserModel = AppModel.shared.currentUser.getUserDetailToSave(){
        UserDefaults.standard.set(user.toJsonData(), forKey: "toShare_user")
        UserDefaults.standard.synchronize()
    }
}

func getSavedUser() -> UserModel?
{
    if let data : Data = UserDefaults.standard.value(forKey: "toShare_user") as? Data
    {
        do{
            let jsonDecoder = JSONDecoder()
            let decodedLog = try jsonDecoder.decode(UserModel.self, from: data)
            return decodedLog
        }
        catch
        {
            return nil
        }
    }
    return nil
}
func removeAllUserDefaultValue()
{
    UserDefaults.standard.removeObject(forKey:"toShare_user")
    //UserDefaults.standard.removePersistentDomain(forName: Bundle.main.bundleIdentifier!)
    UserDefaults.standard.synchronize()
}

//MARK:- Refine para function
func saveSearchRefineBrakeTypesPara(){
    let date:String = getDateStringFromDate(Date(), format: DATE_FORMAT.DATE)
    UserDefaults.standard.set(HOME_CONSTANT.BRAKE_TYPE.toJsonData(), forKey: date + "_brakeTypes")
    UserDefaults.standard.synchronize()
}
func getSearchRefineBrakeTypesPara() -> BrakeTypeModelList?{
    let date:String = getDateStringFromDate(Date(), format: DATE_FORMAT.DATE)
    if let data : Data = UserDefaults.standard.value(forKey: date + "_brakeTypes") as? Data
    {
        do{
            let jsonDecoder = JSONDecoder()
            let decodedLog = try jsonDecoder.decode(BrakeTypeModelList.self, from: data)
            return decodedLog
        }
        catch
        {
            return nil
        }
    }
    else{
        return nil
    }
}

func saveSearchRefineConnectorTypesPara()
{
    let date:String = getDateStringFromDate(Date(), format: DATE_FORMAT.DATE)
    UserDefaults.standard.set(HOME_CONSTANT.CONNECTOR_TYPE.toJsonData(), forKey: date + "_conenctorTypes")
    UserDefaults.standard.synchronize()
}
func getSearchRefineConnectorTypesPara() -> ConnectorTypeModelList?{
    let date:String = getDateStringFromDate(Date(), format: DATE_FORMAT.DATE)
    if let data : Data = UserDefaults.standard.value(forKey: date + "_conenctorTypes") as? Data
    {
        do{
            let jsonDecoder = JSONDecoder()
            let decodedLog = try jsonDecoder.decode(ConnectorTypeModelList.self, from: data)
            return decodedLog
        }
        catch
        {
            return nil
        }
    }
    else{
        return nil
    }
}

func saveSearchRefineRecentKeywords(){
    if !(AppModel.shared.refinePara.recentKeywords.contains(AppModel.shared.refinePara.keyword)){
        AppModel.shared.refinePara.recentKeywords.append(AppModel.shared.refinePara.keyword)
    }
}

func saveSearchRefinePara()
{
    saveSearchRefineRecentKeywords()
    UserDefaults.standard.set(AppModel.shared.refinePara.toJsonData(), forKey: "toShare_refinePara")
    UserDefaults.standard.synchronize()
}

func getSearchRefinePara() -> RefineParaModel?
{
    if let data : Data = UserDefaults.standard.value(forKey: "toShare_refinePara") as? Data
    {
        do{
            let jsonDecoder = JSONDecoder()
            let decodedLog = try jsonDecoder.decode(RefineParaModel.self, from: data)
            return decodedLog
        }
        catch
        {
            return nil
        }
    }
    else{
        return nil
    }
}


//MARK:- Image Function
func compressImage(_ image: UIImage, to toSize: CGSize) -> UIImage {
    var actualHeight: Float = Float(image.size.height)
    var actualWidth: Float = Float(image.size.width)
    let maxHeight: Float = Float(toSize.height)
    //600.0;
    let maxWidth: Float = Float(toSize.width)
    //800.0;
    var imgRatio: Float = actualWidth / actualHeight
    let maxRatio: Float = maxWidth / maxHeight
    //50 percent compression
    if actualHeight > maxHeight || actualWidth > maxWidth {
        if imgRatio < maxRatio {
            //adjust width according to maxHeight
            imgRatio = maxHeight / actualHeight
            actualWidth = imgRatio * actualWidth
            actualHeight = maxHeight
        }
        else if imgRatio > maxRatio {
            //adjust height according to maxWidth
            imgRatio = maxWidth / actualWidth
            actualHeight = imgRatio * actualHeight
            actualWidth = maxWidth
        }
        else {
            actualHeight = maxHeight
            actualWidth = maxWidth
        }
    }
    let rect = CGRect(x: CGFloat(0.0), y: CGFloat(0.0), width: CGFloat(actualWidth), height: CGFloat(actualHeight))
    UIGraphicsBeginImageContext(rect.size)
    image.draw(in: rect)
    let img: UIImage? = UIGraphicsGetImageFromCurrentImageContext()
    let imageData1: Data? = UIImageJPEGRepresentation(img!, CGFloat(CONSTANT.IMAGE_QUALITY))
    UIGraphicsEndImageContext()
    return  imageData1 == nil ? image : UIImage(data: imageData1!)!
}

//MARK:- UI Function
func getTableBackgroundViewForNoData(_ str:String, size:CGSize) -> UIView{
    let noDataLabel: UILabel     = UILabel(frame: CGRect(x: 0, y: 0, width: size.width, height: size.height))
    noDataLabel.text          = str.decoded
    noDataLabel.textColor     = DarkGrayColor
    noDataLabel.font          = Regular18Font
    noDataLabel.textAlignment = .center
    return noDataLabel
}
func showCreditFormattedStr(_ credit:Int?) -> String{
    if(credit == nil){
        return "$0"
    }
    else{
        return "$" + String(credit!)
    }
}

func showEmailFormattedStr(_ str:String) -> String{
    let  arr:[String] = str.components(separatedBy: "@")
    if(arr.count == 2){
        if(arr[0].count > 2){
            return arr[0][0] + "***" + arr[0][arr[0].count-1] + arr[1]
        }
        else{
            return str
        }
    }
    return str
}
func showCardNumberFormattedStr(_ str:String, isRedacted:Bool = true) -> String{
    
    let tempStr:String = sendDetailByRemovingChar(sendDetailByRemovingChar(str, char:"-"), char: " ")
    var retStr:String = ""
    for i in 0..<tempStr.count{
        if(i == 4 || i == 8 || i == 12){
            retStr += "-"
        }
        retStr += tempStr[i]
    }
    if(isRedacted){
        var arr:[String] = retStr.components(separatedBy: "-")
        for i in 0..<arr.count{
            if(i == 1 || i == 2){
                arr[i] = "xxxx"
            }
        }
        retStr = arr.joined(separator: "-")
    }
    return retStr
}
func showCardExpDateFormattedStr(_ str:String) -> String{
    
    let tempStr:String = sendDetailByRemovingChar(str, char:"/")
    var retStr:String = ""
    for i in 0..<tempStr.count{
        if(i == 2){
            retStr += "/"
        }
        retStr += tempStr[i]
    }
    return retStr
}
func showMobileFormattedStr(_ str:String, font:UIFont) -> NSMutableAttributedString{
   
    let tempStr:String = str == "" || str.count < 2 ? CONSTANT.MOBILE_NUMBER_CODE : sendDetailByRemovingChar(str)
    var mobileNum:String = ""
    var attributedString:NSMutableAttributedString!
    
    var i:Int = 0
    for char in tempStr {
        if(i == 4 || i == 7){
            mobileNum = mobileNum + " " + String(char)
        }
        else{
            mobileNum = mobileNum + String(char)
        }
        i = i + 1
    }
    attributedString = NSMutableAttributedString(string: mobileNum)
    if(attributedString.length >= 2){
        attributedString.addAttribute(NSAttributedStringKey.font, value: Bold16Font, range: NSMakeRange(0, 2))
    }
    if(attributedString.length >= 3){
        attributedString.addAttribute(NSAttributedStringKey.underlineColor, value: LightBorderColor, range: NSMakeRange(2,attributedString.length-2))
        attributedString.addAttribute(NSAttributedStringKey.underlineStyle, value: 1, range: NSMakeRange(2,attributedString.length-2))
    }
    
    
    return attributedString
}
func showDOBFormattedStr(_ str:String, font:UIFont) -> NSMutableAttributedString{
    
    let tempStr:String = sendDetailByRemovingChar(str)
    var DOB:String = ""
    var attributedString:NSMutableAttributedString!
    
    var i:Int = 0
    for char in tempStr {
        if(i == 2 || i == 4){
            DOB = DOB + "  " + String(char)
        }
        else{
            DOB = DOB + String(char)
        }
        i = i + 1
    }
    attributedString = NSMutableAttributedString(string: DOB)
    if(attributedString.length > 0){
        attributedString.addAttribute(NSAttributedStringKey.underlineColor, value: LightBorderColor, range: NSMakeRange(0,attributedString.length-0))
        attributedString.addAttribute(NSAttributedStringKey.underlineStyle, value: 1, range: NSMakeRange(0,attributedString.length-0))
    }
    return attributedString
}
func showCodeFormattedStr(_ str:String, font:UIFont) -> NSMutableAttributedString{
    
    let tempStr:String = sendDetailByRemovingChar(str)
    var code:String = ""
    var attributedString:NSMutableAttributedString!
    
    var i:Int = 0
    for char in tempStr {
        if(i != 0){
            code = code + " " + String(char)
        }
        else{
            code = code + String(char)
        }
        i = i + 1
    }
    attributedString = NSMutableAttributedString(string: code)
    if(attributedString.length > 0){
        attributedString.addAttribute(NSAttributedStringKey.underlineColor, value: LightBorderColor, range: NSMakeRange(0,attributedString.length-0))
        attributedString.addAttribute(NSAttributedStringKey.underlineStyle, value: 1, range: NSMakeRange(0,attributedString.length-0))
    }
    return attributedString
}
func sendDetailByRemovingChar(_ str:String, char:String = " ") -> String{
    let regExp :String = char + "\n\t\r"
    return String(str.filter { !(regExp.contains($0))})
}
func sendDetailByRemovingChar(_ attrStr:NSAttributedString, char:String = " ") -> String{
    let str:String = attrStr.string
    let regExp :String = char + "\n\t\r"
    return String(str.filter { !(regExp.contains($0))})
}
func setUserProfileImage(_ user : UserModel, button : Button)
{
    if let image : UIImage = AppModel.shared.usersAvatar[user.Id]{
        button.setBackgroundImage(image, for: .normal)
    }
    else{
        button.setBackgroundImage(UIImage(named:"user_placeholder"), for: .normal)
        APIManager.shared.serviceCallToGetUserAvatar(user, btn: button)
    }
}

func displayToast(_ message:String)
{
    AppDelegate().sharedDelegate().window?.makeToast(message)
    if(AppDelegate().sharedDelegate().isKeyboardOpen){
        UIApplication.shared.windows.last!.makeToast(message)
    }
}
func showLoader()
{
    AppDelegate().sharedDelegate().showLoader()
}
func removeLoader()
{
    AppDelegate().sharedDelegate().removeLoader()
}
func showAlertWithOption(_ title:String, message:String, btns:[String] = ["Yes", "Cancel"],completionConfirm: @escaping () -> Void,completionCancel: @escaping () -> Void){
    let myAlert = UIAlertController(title:title, message:message, preferredStyle: UIAlertControllerStyle.alert)
    let rightBtn = UIAlertAction(title: btns[0], style: UIAlertActionStyle.default, handler: { (action) in
        completionConfirm()
    })
    let leftBtn = UIAlertAction(title: btns[1], style: UIAlertActionStyle.cancel, handler: { (action) in
        completionCancel()
    })
    myAlert.addAction(rightBtn)
    myAlert.addAction(leftBtn)
    AppDelegate().sharedDelegate().window?.rootViewController?.present(myAlert, animated: true, completion: nil)
}

func showAlert(_ title:String, message:String, completion: @escaping () -> Void){
    let myAlert = UIAlertController(title:title, message:message, preferredStyle: UIAlertControllerStyle.alert)
    let okAction = UIAlertAction(title: "OK", style: UIAlertActionStyle.cancel, handler:{ (action) in
        completion()
    })
    myAlert.addAction(okAction)
    AppDelegate().sharedDelegate().window?.rootViewController?.present(myAlert, animated: true, completion: nil)
}

func displaySubViewtoParentView(_ parentview: UIView! , subview: UIView!)
{
    subview.translatesAutoresizingMaskIntoConstraints = false
    parentview.addSubview(subview);
    parentview.addConstraint(NSLayoutConstraint(item: subview, attribute: NSLayoutAttribute.top, relatedBy: NSLayoutRelation.equal, toItem: parentview, attribute: NSLayoutAttribute.top, multiplier: 1.0, constant: 0.0))
    parentview.addConstraint(NSLayoutConstraint(item: subview, attribute: NSLayoutAttribute.leading, relatedBy: NSLayoutRelation.equal, toItem: parentview, attribute: NSLayoutAttribute.leading, multiplier: 1.0, constant: 0.0))
    parentview.addConstraint(NSLayoutConstraint(item: subview, attribute: NSLayoutAttribute.bottom, relatedBy: NSLayoutRelation.equal, toItem: parentview, attribute: NSLayoutAttribute.bottom, multiplier: 1.0, constant: 0.0))
    parentview.addConstraint(NSLayoutConstraint(item: subview, attribute: NSLayoutAttribute.trailing, relatedBy: NSLayoutRelation.equal, toItem: parentview, attribute: NSLayoutAttribute.trailing, multiplier: 1.0, constant: 0.0))
    parentview.layoutIfNeeded()
}

func displaySubViewWithScaleOutAnim(_ view:UIView){
    view.transform = CGAffineTransform(scaleX: 0.4, y: 0.4)
    view.alpha = 1
    UIView.animate(withDuration: 0.35, delay: 0.0, usingSpringWithDamping: 0.55, initialSpringVelocity: 1.0, options: [], animations: {() -> Void in
        view.transform = CGAffineTransform.identity
    }, completion: {(_ finished: Bool) -> Void in
    })
}
func displaySubViewWithScaleInAnim(_ view:UIView){
    UIView.animate(withDuration: 0.25, animations: {() -> Void in
        view.transform = CGAffineTransform(scaleX: 0.65, y: 0.65)
        view.alpha = 0.0
    }, completion: {(_ finished: Bool) -> Void in
        view.removeFromSuperview()
    })
}

//MARK:- Color function
func colorFromHex(hex : String) -> UIColor
{
    return colorWithHexString(hex, alpha: 1.0)
}

func colorFromHex(hex : String, alpha:CGFloat) -> UIColor
{
    return colorWithHexString(hex, alpha: alpha)
}

func colorWithHexString(_ stringToConvert:String, alpha:CGFloat) -> UIColor {
    
    var cString:String = stringToConvert.trimmingCharacters(in: .whitespacesAndNewlines).uppercased()
    
    if (cString.hasPrefix("#")) {
        cString.remove(at: cString.startIndex)
    }
    
    if ((cString.count) != 6) {
        return UIColor.gray
    }
    
    var rgbValue:UInt32 = 0
    Scanner(string: cString).scanHexInt32(&rgbValue)
    
    return UIColor(
        red: CGFloat((rgbValue & 0xFF0000) >> 16) / 255.0,
        green: CGFloat((rgbValue & 0x00FF00) >> 8) / 255.0,
        blue: CGFloat(rgbValue & 0x0000FF) / 255.0,
        alpha: alpha
    )
}

func timeAgoforDate (toDate: String?, withTimeZone timezoneStr: String?) -> String?
{
    var timeAgoStr: String
    let formatter = DateFormatter()
    formatter.dateFormat = "yyyy-MM-dd HH:mm:ss"
//    let gmt = NSTimeZone(abbreviation: timezoneStr ?? "")
//
//    if let aGmt = gmt
//    {
//        formatter.timeZone = aGmt as TimeZone
//    }
    
    let date: Date? = formatter.date(from: toDate ?? "")
    let today = Date()
    let intervalTime = TimeInterval(fabs(Float(date?.timeIntervalSince(today) ?? 0.0)))
    let secondsInterval = Int(floor(intervalTime))
    let minutesInterval = Int(floor(intervalTime / 60.0))
    let hoursInterval = Int(floor(intervalTime / (60.0 * 60.0)))
    let daysInterval = Int(floor(intervalTime / (60.0 * 60.0 * 24.0)))
    let weeksInterval = Int(floor(intervalTime / (60.0 * 60.0 * 24.0 * 7)))
    let monthsInterval = Int(floor(intervalTime / (60.0 * 60.0 * 24.0 * 30)))
    let yearsInterval = Int(floor(intervalTime / (60.0 * 60.0 * 24.0 * 30 * 365)))
    var period: String
    
    if secondsInterval < 3
    {
        timeAgoStr = "Just now"
    }
    else if secondsInterval < 60
    {
        period = (secondsInterval > 1) ? "seconds" : "second"
        timeAgoStr = "\(Int(secondsInterval)) \(period) ago"
    }
    else if minutesInterval < 60
    {
        period = (minutesInterval > 1) ? "minutes" : "minute"
        timeAgoStr = "\(Int(minutesInterval)) \(period) ago"
    }
    else if hoursInterval < 24
    {
        period = (hoursInterval > 1) ? "hours" : "hour"
        timeAgoStr = "\(Int(hoursInterval)) \(period) ago"
    }
    else if hoursInterval >= 24 && hoursInterval < 48
    {
        timeAgoStr = "Yesterday"
    }
    else if daysInterval < 7
    {
        period = (daysInterval > 1) ? "days" : "day"
        timeAgoStr = "\(Int(daysInterval)) \(period) ago"
    }
    else if monthsInterval < 1
    {
        period = (weeksInterval > 1) ? "weeks" : "week"
        timeAgoStr = "\(Int(weeksInterval)) \(period) ago"
    }
    else if monthsInterval < 12
    {
        period = (monthsInterval > 1) ? "months" : "month"
        timeAgoStr = "\(Int(monthsInterval)) \(period) ago"
    }
    else
    {
        period = (yearsInterval > 1) ? "years" : "year"
        timeAgoStr = "\(Int(yearsInterval)) \(period) ago"
    }
    return timeAgoStr
}


//MARK:- Features
func delay(_ delay:Double, closure:@escaping ()->()) {
    DispatchQueue.main.asyncAfter(
        deadline: DispatchTime.now() + Double(Int64(delay * Double(NSEC_PER_SEC))) / Double(NSEC_PER_SEC), execute: closure)
}

