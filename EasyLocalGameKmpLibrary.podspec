Pod::Spec.new do |spec|
  spec.name                     = 'EasyLocalGameKmpLibrary'
  spec.version                  = '1.0.0'
  spec.summary                  = 'Kotlin Multiplatform library for local multiplayer games'
  spec.homepage                 = 'https://github.com/grasski/EasyLocalGameKmp'
  spec.license                  = { :type => 'Apache-2.0' }
  spec.author                   = { 'Your Name' => 'your@email.com' }
  spec.source                   = { :git => 'https://github.com/grasski/EasyLocalGameKmp.git', :tag => spec.version }
  
  spec.ios.deployment_target    = '14.0'
  spec.static_framework         = true
  
  # Include the Swift source files
  spec.source_files             = 'EasyLocalGameKmpLibrary/src/iosMain/swift/**/*.swift'
  
  # The prebuilt Kotlin framework (users build this locally or download from releases)
  spec.vendored_frameworks      = 'EasyLocalGameKmpLibrary/build/bin/iosArm64/releaseFramework/EasyLocalGameKmpLibraryKit.framework'
  
  # Dependency on NearbyConnections
  spec.dependency 'NearbyConnections'
  
  spec.pod_target_xcconfig = {
    'KOTLIN_PROJECT_PATH' => ':EasyLocalGameKmpLibrary',
    'PRODUCT_MODULE_NAME' => 'EasyLocalGameKmpLibrary',
  }
end
