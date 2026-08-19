import Foundation
import DeclaredAgeRange
import UIKit
import NitroModules

class PlayAgeRangeDeclaration: HybridPlayAgeRangeDeclarationSpec {

  func isEligibleForAgeFeatures() throws -> Promise<Bool> {
    return Promise.async {
      guard #available(iOS 26.2, *) else { return false }
      return try await AgeRangeService.shared.isEligibleForAgeFeatures
    }
  }

  func requestDeclaredAgeRange(
    firstThresholdAge: Double,
    secondThresholdAge: Double?,
    thirdThresholdAge: Double?
  ) throws -> Promise<DeclaredAgeRangeResult> {

    return Promise.async {
      // On iOS < 26.2 the API doesn't exist. Return a structured ineligible
      // result rather than throwing — the JS layer can tell "we can't ask"
      // from "we asked and got no answer" by looking at isEligible.
      guard #available(iOS 26.2, *) else {
        return DeclaredAgeRangeResult(
          isEligible: false,
          status: nil,
          parentControls: nil,
          lowerBound: nil,
          upperBound: nil
        )
      }

      // NOTE: Deliberately no isEligibleForAgeFeatures pre-check here.
      // Callers who want that gate should call isEligibleForAgeFeatures()
      // first; this method always tries to present the sheet.

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

  func setGooglePlayMockUser(config: PlayAgeSignalsMockConfig?) throws {
    // No-op on iOS — Google Play Age Signals API is Android-only
  }

  func setAmazonMockScenario(scenario: Double?) throws {
    // No-op on iOS — Amazon Appstore Age API is Android-only
  }

  func setSamsungMockScenario(scenario: Double?) throws {
    // No-op on iOS — Samsung Galaxy Store Age API is Android-only
  }

  func detectStore() -> AppStore {
    return .appleAppstore
  }

  func getAmazonUserAgeData() throws -> Promise<AmazonGetUserAgeDataResult> {
    return Promise<AmazonGetUserAgeDataResult>.rejected(
      withError: NSError(
        domain: "PlayAgeRangeDeclaration", code: -1,
        userInfo: [NSLocalizedDescriptionKey: "Amazon Appstore age signals are not available on iOS"]
      )
    )
  }

  func getSamsungAgeSignals() throws -> Promise<SamsungGetAgeSignalsResult> {
    return Promise<SamsungGetAgeSignalsResult>.rejected(
      withError: NSError(
        domain: "PlayAgeRangeDeclaration", code: -1,
        userInfo: [NSLocalizedDescriptionKey: "Samsung Galaxy Store age signals are not available on iOS"]
      )
    )
  }

  func getGooglePlayAgeSignals() throws -> Promise<PlayAgeSignalsResult> {
    return Promise<PlayAgeSignalsResult>.rejected(
      withError: NSError(
        domain: "PlayAgeRangeDeclaration", code: -1,
        userInfo: [NSLocalizedDescriptionKey: "Google Play age signals are not available on iOS"]
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