//
//  GradientLayer.swift
//
//
//  Created by Keyur on 2/1/18.
//  Copyright © 2018 Keyur. All rights reserved.
//

import UIKit

class GradientLayer: CAGradientLayer {

    override func layoutSublayers() {
        super.layoutSublayers()
        
        frame = super.bounds
    }
}
