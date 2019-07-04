//  Created by Keyur on 13/07/17.
//  Copyright © 2017 Keyur. All rights reserved.
//

import Foundation
import SystemConfiguration
import Alamofire
import AlamofireImage
import AlamofireJsonToObjects

struct JSONStringEncoding: ParameterEncoding {
    private let myString: String
    
    init(string: String) {
        self.myString = string
    }
    
    func encode(_ urlRequest: URLRequestConvertible, with parameters: Parameters?) throws -> URLRequest {
        var urlRequest = urlRequest.urlRequest
        
        let data = myString.data(using: .utf8)!
        
        if urlRequest?.value(forHTTPHeaderField: "Content-Type") == nil {
            urlRequest?.setValue("application/json", forHTTPHeaderField: "Content-Type")
        }
        urlRequest?.httpBody = data
        return urlRequest!
    }
}

public class APIManager {
    
    static let shared = APIManager()
    
    class func isConnectedToNetwork() -> Bool {
        var zeroAddress = sockaddr_in()
        zeroAddress.sin_len = UInt8(MemoryLayout.size(ofValue: zeroAddress))
        zeroAddress.sin_family = sa_family_t(AF_INET)
        let defaultRouteReachability = withUnsafePointer(to: &zeroAddress) {
            $0.withMemoryRebound(to: sockaddr.self, capacity: 1) {zeroSockAddress in
                SCNetworkReachabilityCreateWithAddress(nil, zeroSockAddress)
            }
        }
        var flags = SCNetworkReachabilityFlags()
        if !SCNetworkReachabilityGetFlags(defaultRouteReachability!, &flags) {
            return false
        }
        let isReachable = (flags.rawValue & UInt32(kSCNetworkFlagsReachable)) != 0
        let needsConnection = (flags.rawValue & UInt32(kSCNetworkFlagsConnectionRequired)) != 0
        return (isReachable && !needsConnection)
    }
    
    func getBase64CredentialsBeforeLogin() -> [String : String]{
        let email : String = "Gguest"
        let pass : String = "guest"
        let credentialData = "\(email):\(pass)".data(using: String.Encoding.utf8)!
        var headerParams :[String : String] = [String : String] ()
        headerParams["Authorization"] = "Basic \(credentialData.base64EncodedString(options: []))"
        return headerParams
    }
    func getBase64CredentialsWhileLogin() -> [String : String]{
        let email : String = "R" + AppModel.shared.currentUser.Email
        let pass : String = AppModel.shared.currentUser.Password
        let credentialData = "\(email):\(pass)".data(using: String.Encoding.utf8)!
        var headerParams :[String : String] = [String : String] ()
        headerParams["Authorization"] = "Basic \(credentialData.base64EncodedString(options: []))"
        return headerParams
    }
    func getBase64CredentialsAfterLogin() -> [String : String]{
        let email : String = "H" + AppModel.shared.currentUser.Email
        let pass : String = AppModel.shared.currentUser.Hash
        let credentialData = "\(email):\(pass)".data(using: String.Encoding.utf8)!
        var headerParams :[String : String] = [String : String] ()
        headerParams["Authorization"] = "Basic \(credentialData.base64EncodedString(options: []))"
        headerParams["Content-Type"] = "application/json"
        
        return headerParams
    }
    
    func isServiceError(_ code: Int?) -> Bool{
        if(code == 401)
        {
            AppDelegate().sharedDelegate().logout()
            return true
        }
        return false
    }
    
    //MARK:- COMMON CALLS
    func serviceCallToGetUserAvatar(_ user:UserModel, btn:Button){
        if(user.AccountPhoto == ""){
            return
        }
        let headerParams :[String : String] = getBase64CredentialsAfterLogin()
       
        Alamofire.request(BASE_URL+"images/"+user.AccountPhoto, method: .get, parameters: nil, encoding: JSONEncoding.default, headers: headerParams).responseImage { (response) in
            
            switch response.result {
            case .success:
                print(response.result.value)
                if let result:UIImage = response.result.value as? UIImage
                {
                    let scaledImage = result.imageResize()
                    btn.setBackgroundImage(scaledImage, for: .normal)
                    AppModel.shared.usersAvatar[user.Id] = scaledImage
                    return
                }
                break
            case .failure(let error):
                print(error)
                break
            }
        }
    }
    func serviceCallToDownloadImage(_ photos:Any,placeHolder:String,  btn:Button){
        
        if let placeHolderImage:UIImage = UIImage(named : placeHolder){
            btn.setBackgroundImage(placeHolderImage.imageCropped(toFit: btn.frame.size), for: .normal)
        }
        else{
            btn.setBackgroundImage(nil, for: .normal)
        }
        
        var photo:String = ""
        if let arr = photos as? [String]{
            for i in 0..<arr.count{
                if let str = arr[0] as? String{
                    if(str != ""){
                        photo = str
                        break;
                    }
                }
            }
        }
       
        if(photo == ""){
            
        }
        else if let image = AppModel.shared.photosQueue[photo] as? UIImage{
            btn.setBackgroundImage(image.imageCropped(toFit: btn.frame.size), for: .normal)
        }
        else if let _ = AppModel.shared.photosQueue[photo] as? Bool{
            
        }
        else{
            AppModel.shared.photosQueue[photo] = true
            
            let headerParams :[String : String] = getBase64CredentialsBeforeLogin()
            
            Alamofire.request(BASE_URL+"images/"+photo, method: .get, parameters: nil, encoding: JSONEncoding.default, headers: headerParams).responseImage { (response) in
                
                switch response.result {
                case .success:
                    print(response.result.value)
                    if let result:UIImage = response.result.value as? UIImage
                    {
                        btn.setBackgroundImage(result.imageCropped(toFit: btn.frame.size), for: .normal)
                        AppModel.shared.photosQueue[photo] = result
                        return
                    }
                    break
                case .failure(let error):
                    print(error)
                    AppModel.shared.photosQueue[photo] = nil
                    break
                }
            }
        }
    }
}
