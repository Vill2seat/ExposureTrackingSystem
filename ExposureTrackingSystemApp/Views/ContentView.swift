import SwiftUI
import SwiftData
import CoreLocation

struct ContentView: View {
    @Environment(\.modelContext) private var modelContext
    @StateObject private var trackingManager = LocationTrackingManager()
    @StateObject private var routeEngine = RouteEngine()

    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                VStack(alignment: .leading, spacing: 10) {
                    Text("Location & Route Engine")
                        .font(.title2)
                        .fontWeight(.semibold)

                    Text("Stage 1")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                VStack(alignment: .leading, spacing: 12) {
                    Text("Tracking status: \(trackingManager.isTracking ? "Active" : "Idle")")
                    Text("Duration: \(routeEngine.currentDurationSeconds, specifier: "%.1f") s")
                    Text("Distance: \(routeEngine.currentDistanceMeters, specifier: "%.1f") m")
                    Text("Average speed: \(routeEngine.currentAverageSpeedKmh, specifier: "%.1f") km/h")
                }
                .font(.body)
                .frame(maxWidth: .infinity, alignment: .leading)

                HStack(spacing: 12) {
                    Button(action: {
                        guard !trackingManager.isTracking else { return }

                        routeEngine.configure(modelContext: modelContext)
                        routeEngine.startNewRoute(name: "Route")
                        trackingManager.onLocationUpdate = { location in
                            routeEngine.appendLocation(location)
                        }
                        trackingManager.requestWhenInUsePermission()
                        trackingManager.startUpdatingLocation()
                    }) {
                        Label("Start", systemImage: "location.fill")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.borderedProminent)

                    Button(action: {
                        routeEngine.stopRoute()
                        trackingManager.stopUpdatingLocation()
                    }) {
                        Label("Stop", systemImage: "stop.circle.fill")
                            .frame(maxWidth: .infinity)
                    }
                    .buttonStyle(.bordered)
                }
            }
            .padding()
            .navigationTitle("ETS")
            .onAppear {
                routeEngine.configure(modelContext: modelContext)
                trackingManager.onLocationUpdate = { location in
                    routeEngine.appendLocation(location)
                }
            }
        }
    }
}

private extension LocationTrackingManager {
    var authorizationStatusString: String {
        authorizationStatus.stringValue
    }
}

private extension CLAuthorizationStatus {
    var stringValue: String {
        switch self {
        case .notDetermined:
            return "Not Determined"
        case .restricted:
            return "Restricted"
        case .denied:
            return "Denied"
        case .authorizedAlways:
            return "Always"
        case .authorizedWhenInUse:
            return "When In Use"
        case .authorized:
            return "Authorized"
        @unknown default:
            return "Unknown"
        }
    }
}

#Preview {
    ContentView()
}
