//
//  LoginSignUpService.swift
//
//
//  Created by Keyur on 2/16/18.
//  Copyright © 2018 Keyur. All rights reserved.
//

import Alamofire
import AlamofireImage
import AlamofireJsonToObjects

class LoginSignupService: NSObject {

    static let shared = LoginSignupService()
    
    //MARK:- login-signup
    func serviceCallToLogin(_ completion: @escaping () -> Void) {
        showLoader()
        
        var headerParams :[String : String] = APIManager.shared.getBase64CredentialsWhileLogin()
        
        headerParams["username"] = AppModel.shared.currentUser.Email
        headerParams["password"] = AppModel.shared.currentUser.Password
        
        Alamofire.request(BASE_URL+"users/login", method: .get, parameters: nil, encoding: JSONEncoding.default, headers: headerParams).responseJSON {
            response in
            
            removeLoader()
        
            switch response.result {
            case .success:
                print(response.result.value)
                if let result = response.result.value  as? [String:Any]
                {
                    if let Id:String = result["Id"] as? String,let Hash:String = result["Hash"] as? String
                    {
                        AppModel.shared.currentUser.Id = Id
                        AppModel.shared.currentUser.Hash = Hash
                        saveUser()
                        completion()
                        return
                    }
                }
                if let error = response.result.error
                {
                    displayToast(error.localizedDescription)
                    return
                }
                displayToast(NSLocalizedString("login_error", comment: ""))
                break
            case .failure(let error):
                print(error)
                displayToast(NSLocalizedString("login_error", comment: ""))
                break
            }
        }
    }
    func serviceCallToRegister(_ completion: @escaping () -> Void){
        showLoader()
        
        let headerParams :[String : String] = APIManager.shared.getBase64CredentialsBeforeLogin()
        
        
        var params :[String : Any] = [String : Any] ()
        params["Email"] = AppModel.shared.currentUser.Email
        params["Password"] = AppModel.shared.currentUser.Password
        params["PreferredName"] = AppModel.shared.currentUser.PreferredName
        params["PreferredName"] = AppModel.shared.currentUser.PreferredName
        params["Referral"] = AppModel.shared.currentUser.Referral
        
        Alamofire.request(BASE_URL+"users/register", method: .post, parameters: params, encoding: JSONEncoding.default, headers: headerParams).responseJSON { (response) in
            
            removeLoader()
            
            switch response.result {
            case .success:
                print(response.result.value)
                if let result = response.result.value as? String
                {
                    if result.lowercased() == "ok"
                    {
                        completion()
                    }
                    else{
                        displayToast(result)
                    }
                    return
                }
                if let error = response.result.error
                {
                    displayToast(error.localizedDescription)
                    return
                }
                displayToast(NSLocalizedString("register_error", comment: ""))
                break
            case .failure(let error):
                print(error)
                displayToast(NSLocalizedString("register_error", comment: ""))
                break
            }
        }
    }
    
    func serviceCallToGetCurrentUser(_ completion: @escaping () -> Void){
        showLoader()
        
        let headerParams :[String : String] = APIManager.shared.getBase64CredentialsAfterLogin()
        Alamofire.request(BASE_URL+"users/"+AppModel.shared.currentUser.Id, method: .get, parameters: nil, encoding: JSONEncoding.default, headers: headerParams).responseObject { (response: DataResponse<UserModel>) in
            
            removeLoader()
            
            if(APIManager.shared.isServiceError(response.response?.statusCode)){
                return
            }
            
            switch response.result {
            case .success:
                print(response.data)
                if let data = response.data
                {
                    do{
                        let jsonDecoder = JSONDecoder()
                        let decodedLog = try jsonDecoder.decode(UserModel.self, from: data)
                        let Hash = AppModel.shared.currentUser.Hash
                        let location = AppModel.shared.currentUser.currLocation
                        AppModel.shared.currentUser = decodedLog
                        AppModel.shared.currentUser.Hash = Hash
                        AppModel.shared.currentUser.currLocation = location
                        saveUser()
                        completion()
                        NotificationCenter.default.post(name:NSNotification.Name(rawValue: NOTIFICATION.ON_UPDATE_USER_DETAIL), object: nil)
                    }
                    catch
                    {
                        displayToast(NSLocalizedString("something_went_wrong", comment: ""))
                    }
                    return
                }
                AppDelegate().sharedDelegate().logout()
                if let error = response.result.error
                {
                    displayToast(error.localizedDescription)
                    return
                }
                displayToast(NSLocalizedString("get_user_error", comment: ""))
                break
            case .failure(let error):
                AppDelegate().sharedDelegate().logout()
                print(error)
                displayToast(NSLocalizedString("get_user_error", comment: ""))
                break
            }
        }
    }
   
    func serviceCallToGetUserAgreement(completion: @escaping (_ str:String) -> Void){
        showLoader()
        
        let headerParams :[String : String] = APIManager.shared.getBase64CredentialsBeforeLogin()
        
        
        Alamofire.request(BASE_URL+"useragreement", method: .get, parameters: nil, encoding: JSONEncoding.default, headers: headerParams).responseJSON {
            response in
            
            removeLoader()
            
            switch response.result {
            case .success:
                print(response.result.value)
                if let result = response.result.value  as? String
                {
                    completion(result)
                    return
                }
                if let error = response.result.error
                {
                    displayToast(error.localizedDescription)
                    return
                }
                displayToast(NSLocalizedString("get_user_agreement_error", comment: ""))
                break
            case .failure(let error):
                print(error)
                displayToast(NSLocalizedString("get_user_agreement_error", comment: ""))
                break
            }
        }
    }
    func serviceCallToGetPrivacyPolicy(completion: @escaping (_ str:String) -> Void){
        showLoader()
        
        let headerParams :[String : String] = APIManager.shared.getBase64CredentialsBeforeLogin()
        
        
        Alamofire.request(BASE_URL+"privacypolicy", method: .get, parameters: nil, encoding: JSONEncoding.default, headers: headerParams).responseJSON {
            response in
            
            removeLoader()
            
            switch response.result {
            case .success:
                print(response.result.value)
                if let result = response.result.value  as? String
                {
                    completion(result)
                    return
                }
                if let error = response.result.error
                {
                    displayToast(error.localizedDescription)
                    return
                }
                displayToast(NSLocalizedString("get_user_agreement_error", comment: ""))
                break
            case .failure(let error):
                print(error)
                displayToast(NSLocalizedString("get_user_agreement_error", comment: ""))
                break
            }
        }
    }
}
