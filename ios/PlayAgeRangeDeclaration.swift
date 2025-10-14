import React
import Foundation
import DeclaredAgeRange
import UIKit
import NitroModules

class PlayAgeRangeDeclaration: HybridPlayAgeRangeDeclarationSpec {

  // ==== Required protocol conformance ====

  func requestDeclaredAgeRange(ageGate: Double) throws -> Promise<DeclaredAgeRangeResult> {
    print("[PlayAgeRangeDeclaration] 🟦 Starting requestDeclaredAgeRange with ageGate=\(ageGate)")

    return Promise.async {
      do {
        // 1️⃣ Check for iOS version
        guard #available(iOS 26.0, *) else {
          let message = "Declared Age Range API requires iOS 26+"
          print("[PlayAgeRangeDeclaration] ❌ \(message)")
          throw NSError(domain: "PlayAgeRangeDeclaration", code: 1,
                        userInfo: [NSLocalizedDescriptionKey: message])
        }

        // 2️⃣ Find top view controller
        guard let viewController = Self.topViewController() else {
          let message = "Could not find top view controller to present UI"
          print("[PlayAgeRangeDeclaration] ❌ \(message)")
          throw NSError(domain: "PlayAgeRangeDeclaration", code: 2,
                        userInfo: [NSLocalizedDescriptionKey: message])
        }

        print("[PlayAgeRangeDeclaration] ✅ Found topViewController: \(viewController)")

        // 3️⃣ Convert and call API
        let intGate = Int(ageGate)
        print("[PlayAgeRangeDeclaration] 🚀 Calling AgeRangeService.shared.requestAgeRange with gate=\(intGate)")

        let response = try await AgeRangeService.shared.requestAgeRange(
          ageGates: intGate,
          in: viewController
        )

        // 4️⃣ Handle response
        switch response {
        case .sharing(let declaration):
          print("[PlayAgeRangeDeclaration] ✅ User shared age range declaration: \(declaration)")
          return DeclaredAgeRangeResult(
            status: "\(declaration.ageRangeDeclaration)",
            lowerBound: declaration.lowerBound.map { Double($0) },
            upperBound: declaration.upperBound.map { Double($0) },
            error: nil
          )

        case .declinedSharing:
          print("[PlayAgeRangeDeclaration] ⚠️ User declined to share age range.")
          return DeclaredAgeRangeResult(
            status: "declined",
            lowerBound: nil,
            upperBound: nil,
            error: nil
          )
        }

      } catch {
        print("[PlayAgeRangeDeclaration] 💥 Error inside requestDeclaredAgeRange: \(error)")
        throw error
      }
    }
  }

  func getPlayAgeRangeDeclaration() throws -> Promise<PlayAgeRangeDeclarationResult> {
    print("[PlayAgeRangeDeclaration] ⚙️ getPlayAgeRangeDeclaration() called — not implemented")
    return Promise<PlayAgeRangeDeclarationResult>.rejected(
      withError: NSError(domain: "PlayAgeRangeDeclaration",
                         code: -1,
                         userInfo: [NSLocalizedDescriptionKey: "Not implemented"])
    )
  }

  // ==== Utilities ====

  private static func topViewController() -> UIViewController? {
    print("[PlayAgeRangeDeclaration] 🧭 Looking for topViewController")
    guard let root = UIApplication.shared.connectedScenes
      .compactMap({ $0 as? UIWindowScene })
      .flatMap({ $0.windows })
      .first(where: { $0.isKeyWindow })?.rootViewController else {
        print("[PlayAgeRangeDeclaration] ❌ No rootViewController found")
        return nil
    }

    var top = root
    while let presented = top.presentedViewController {
      print("[PlayAgeRangeDeclaration] 🔄 Found presentedViewController: \(presented)")
      top = presented
    }

    print("[PlayAgeRangeDeclaration] 🟢 Final topViewController: \(top)")
    return top
  }
}
