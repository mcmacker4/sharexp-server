import SwiftUI

@main
struct iOSApp: App {
    init() {
        KoinModule.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}