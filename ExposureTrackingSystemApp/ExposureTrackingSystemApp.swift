import SwiftUI
import SwiftData

@main
struct ExposureTrackingSystemApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .modelContainer(for: [LocationPoint.self, Route.self, RouteSegment.self])
        }
    }
}
