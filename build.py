#!/usr/bin/env python3
"""
Build script for Sharma Khata & Billing Android App
"""
import os
import subprocess
import sys
import shutil
from pathlib import Path

def run_command(cmd, cwd=None):
    """Run a command and return the result"""
    print(f"\n>>> {' '.join(cmd)}\n")
    result = subprocess.run(cmd, cwd=cwd, capture_output=False)
    return result.returncode

def main():
    # Get project root
    script_dir = Path(__file__).parent.absolute()
    project_root = script_dir
    
    print("=" * 60)
    print("Sharma Khata & Billing Android Build")
    print("=" * 60)
    
    # Check requirements
    print("\n1. Checking environment...")
    
    # Check for gradlew.bat
    gradlew = project_root / "gradlew.bat"
    if not gradlew.exists():
        print(f"   - Creating Gradle wrapper...")
        # Create gradle wrapper directory
        wrapper_dir = project_root / "gradle" / "wrapper"
        wrapper_dir.mkdir(parents=True, exist_ok=True)
        
        # Create gradle-wrapper.properties
        props_content = """distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\\://services.gradle.org/distributions/gradle-8.9-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
"""
        props_file = wrapper_dir / "gradle-wrapper.properties"
        props_file.write_text(props_content)
        print(f"   ✓ Created {props_file}")
    else:
        print(f"   ✓ Gradle wrapper found")
    
    # Check Java
    print("\n2. Checking Java...")
    ret = subprocess.run([sys.executable, "-c", "import subprocess; subprocess.run(['java', '-version'])"], 
                        capture_output=True)
    if ret.returncode == 0:
        print("   ✓ Java is available")
    else:
        print("   ✗ Java not found - please install JDK")
        return 1
    
    # Build the app
    print("\n3. Building Android app...")
    print("   This may take several minutes...")
    
    # Determine gradlew command
    if os.name == 'nt':
        gradle_cmd = ["gradlew.bat", "build"]
    else:
        gradle_cmd = ["./gradlew", "build"]
    
    # Try with gradlew.bat if on Windows
    if os.name == 'nt' and gradlew.exists():
        ret = run_command([str(gradlew), "build"], cwd=str(project_root))
    else:
        # Fall back to system gradle
        ret = run_command(["gradle", "build"], cwd=str(project_root))
    
    if ret == 0:
        print("\n" + "=" * 60)
        print("✓ Build Successful!")
        print("=" * 60)
        
        # Show APK locations
        debug_apk = project_root / "app" / "build" / "outputs" / "apk" / "debug" / "app-debug.apk"
        print(f"\nDebug APK: {debug_apk}")
        if debug_apk.exists():
            print(f"Size: {debug_apk.stat().st_size / (1024*1024):.2f} MB")
        
        return 0
    else:
        print("\n" + "=" * 60)
        print("✗ Build Failed")
        print("=" * 60)
        return 1

if __name__ == "__main__":
    sys.exit(main())
