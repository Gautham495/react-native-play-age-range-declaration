import Foundation
import DeclaredAgeRange
import UIKit
import NitroModules

class PlayAgeRangeDeclaration: HybridPlayAgeRangeDeclarationSpec {

  func requestDeclaredAgeRange(firstThresholdAge: Double, secondThresholdAge: Double, thirdThresholdAge: Double) throws -> Promise<DeclaredAgeRangeResult> {

    return Promise.async {
      do {
        guard #available(iOS 26.0, *) else {
          let message = "Declared Age Range API requires iOS 26+"
          throw NSError(domain: "PlayAgeRangeDeclaration", code: 1,
                        userInfo: [NSLocalizedDescriptionKey: message])
        }

        guard let viewController = Self.topViewController() else {
          let message = "Could not find top view controller to present UI"
          throw NSError(domain: "PlayAgeRangeDeclaration", code: 2,
                        userInfo: [NSLocalizedDescriptionKey: message])
        }

        let firstThreshold = Int(firstThresholdAge)

        let secondThreshold = Int(secondThresholdAge)

        let thirdThreshold = Int(thirdThresholdAge)

        let response = try await AgeRangeService.shared.requestAgeRange(
          ageGates: firstThreshold, secondThreshold, thirdThreshold,
          in: viewController
        )

        switch response {

                case .sharing(let declaration):
                  
                  let statusString: String

                  if let declarationStatus = declaration.ageRangeDeclaration {
                      statusString = String(describing: declarationStatus)
                
                  } else {
                      statusString = "unknown"
                  }
          
                  let controlsRawValue = declaration.activeParentalControls.rawValue
                  
                  return DeclaredAgeRangeResult(
                    status: statusString,
                    parentControls: "\(controlsRawValue)",
                    lowerBound: declaration.lowerBound.map { Double($0) },
                    upperBound: declaration.upperBound.map { Double($0) }
                  )
              
                case .declinedSharing:
                  return DeclaredAgeRangeResult(
                    status: "declined",
                    parentControls: nil,
                    lowerBound: nil,
                    upperBound: nil
                  )
                  
      }

      } catch {
        throw error
      }
    }
  }

  func getPlayAgeRangeDeclaration() throws -> Promise<PlayAgeRangeDeclarationResult> {

    return Promise<PlayAgeRangeDeclarationResult>.rejected(
      withError: NSError(domain: "PlayAgeRangeDeclaration",
                         code: -1,
                         userInfo: [NSLocalizedDescriptionKey: "Not implemented"])
    )
  }

  private static func topViewController() -> UIViewController? {
    guard let root = UIApplication.shared.connectedScenes
      .compactMap({ $0 as? UIWindowScene })
      .flatMap({ $0.windows })
      .first(where: { $0.isKeyWindow })?.rootViewController else {
        return nil
    }

    var top = root
    while let presented = top.presentedViewController {
      top = presented
    }
    return top
  }
}

