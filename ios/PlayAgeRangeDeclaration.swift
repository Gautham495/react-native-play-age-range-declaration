import Foundation
import DeclaredAgeRange
import UIKit
import NitroModules

class PlayAgeRangeDeclaration: HybridPlayAgeRangeDeclarationSpec {

  func requestDeclaredAgeRange(
    firstThresholdAge: Double,
    secondThresholdAge: Double?,
    thirdThresholdAge: Double?
  ) throws -> Promise<DeclaredAgeRangeResult> {

    return Promise.async {
      guard #available(iOS 26.2, *) else {
        throw NSError(
          domain: "PlayAgeRangeDeclaration", code: 1,
          userInfo: [NSLocalizedDescriptionKey: "Declared Age Range API requires iOS 26.2"]
        )
      }

      // isEligibleForAgeFeatures is a synchronous property, not async/throws
    guard try await AgeRangeService.shared.isEligibleForAgeFeatures else {
  return DeclaredAgeRangeResult(
    isEligible: false,
    status: nil,
    parentControls: nil,
    lowerBound: nil,
    upperBound: nil
  )
}

      guard let viewController = await Self.topViewController() else {
        throw NSError(
          domain: "PlayAgeRangeDeclaration", code: 2,
          userInfo: [NSLocalizedDescriptionKey: "Could not find top view controller to present UI"]
        )
      }

      let firstThreshold = Int(firstThresholdAge)
      let secondThreshold = secondThresholdAge.map { Int($0) }
      let thirdThreshold = thirdThresholdAge.map { Int($0) }

      let response = try await AgeRangeService.shared.requestAgeRange(
        ageGates: firstThreshold, secondThreshold, thirdThreshold,
        in: viewController
      )

      switch response {
     case .sharing(let declaration):
  let status: AppleAgeRangeDeclarationUserStatusValues
  if let declarationStatus = declaration.ageRangeDeclaration {
    // String(describing:) on the Apple enum gives "selfDeclared", "guardianDeclared", etc.
    // which matches the keys in fromString
    status = AppleAgeRangeDeclarationUserStatusValues(
      fromString: String(describing: declarationStatus)
    ) ?? .unknown
  } else {
    status = .unknown
  }

  let controlsRawValue = declaration.activeParentalControls.rawValue

  return DeclaredAgeRangeResult(
    isEligible: true,
    status: status,
    parentControls: "\(controlsRawValue)",
    lowerBound: declaration.lowerBound.map { Double($0) },
    upperBound: declaration.upperBound.map { Double($0) }
  )

      case .declinedSharing:
        return DeclaredAgeRangeResult(
          isEligible: true,
          status: .declined,
          parentControls: nil,
          lowerBound: nil,
          upperBound: nil
        )

      @unknown default:
        return DeclaredAgeRangeResult(
          isEligible: true,
          status: .unknown,
          parentControls: nil,
          lowerBound: nil,
          upperBound: nil
        )
      }
    }
  }

  func getPlayAgeRangeDeclaration() throws -> Promise<PlayAgeRangeDeclarationResult> {
    return Promise<PlayAgeRangeDeclarationResult>.rejected(
      withError: NSError(
        domain: "PlayAgeRangeDeclaration", code: -1,
        userInfo: [NSLocalizedDescriptionKey: "Not implemented"]
      )
    )
  }

  @MainActor
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