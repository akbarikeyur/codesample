//
//  AppModel.swift
//
//
//  Created by Keyur on 1/2/18.
//  Copyright © 2018 Keyur. All rights reserved.
//

import UIKit
import EVReflection
import CoreLocation

class AppModel: EVObject {
    static let shared = AppModel()
    
    var currentUser : UserModel!
    var usersAvatar:[String:UIImage] = [String:UIImage]()
    var photosQueue:[String:Any] = [String:Any]()
    var refinePara:RefineParaModel = RefineParaModel.init()
}
