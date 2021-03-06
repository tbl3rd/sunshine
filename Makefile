ANDROID := $(ANDROID_HOME)/tools/android
ADB := $(ANDROID_HOME)/platform-tools/adb

API := android-19

ANDROIDAPIS := $(API) # Add others as required.

ANDROIDSOURCES := $(addprefix $(ANDROID_HOME)/sources/,$(ANDROIDAPIS))

PACKAGE := com.example.android.sunshine

MAYBE_RESET_USB := test "$(ANDROID_USB)" && $(ADB) usb && sleep 1 || true

# Task names extracted from output of './gradlew tasks' and re-ordered
# slightly.
#
GRADLETASKS :=\
	help\
	tasks\
	androidDependencies\
	signingReport\
	assemble\
	assembleDebug\
	assembleDebugTest\
	assembleRelease\
	build\
	buildDependents\
	buildNeeded\
	clean\
	init\
	wrapper\
	dependencies\
	dependencyInsight\
	projects\
	properties\
	installDebug\
	installDebugTest\
	uninstallAll\
	uninstallDebug\
	uninstallDebugTest\
	uninstallRelease\
	check\
	connectedAndroidTest\
	connectedCheck\
	deviceCheck\
	#


$(GRADLETASKS):
	./gradlew $@
.PHONY: $(GRADLETASKS)


debug: installDebug
	$(MAYBE_RESET_USB)
	$(ADB) logcat -c
	$(ADB) shell pm path $(PACKAGE)
	$(ADB) shell am start -n $(PACKAGE)/$(PACKAGE).MainActivity
	hey you
	$(ADB) logcat
	# $(ADB) shell pm dump $(PACKAGE)
.PHONY: debug


INSTRUMENT := $(PACKAGE).test/android.test.InstrumentationTestRunner
DEBUG_TEST_APK := app/build/outputs/apk/app-debug-test-unaligned.apk

test:
	./gradlew installDebugTest installDebug
	$(MAYBE_RESET_USB)
	@echo
	@echo Look for: Test results for InstrumentationTestRunner=.....
	@echo Look for: 'OK (5 tests)' ... for some value of 5
	@echo
	$(ADB) logcat -c
	$(ADB) shell pm path $(PACKAGE)
	$(ADB) shell pm path $(PACKAGE).test
	$(ADB) shell pm list instrumentation
	$(ADB) shell pm list instrumentation -f
	$(ADB) shell am instrument -w -e target $(PACKAGE) $(INSTRUMENT)
	$(ADB) shell pm uninstall $(PACKAGE).test
	$(ADB) shell pm uninstall $(PACKAGE)
	$(ADB) logcat
.PHONY: test


dump: installDebug
	$(MAYBE_RESET_USB)
	$(ADB) shell pm path $(PACKAGE)
	$(ADB) shell pm dump $(PACKAGE)
.PHONY: dump


monitor:
	$(ANDROID_HOME)/tools/monitor
.PHONY: monitor


bugreport:
	$(MAYBE_RESET_USB)
	$(ADB) bugreport > bugreport.txt
	gzip bugreport.txt
.PHONY: bugreport


lint lintDebug lintRelease:
	./gradlew $@
	open app/build/outputs/lint-results.html
.PHONY: lint lintDebug lintRelease


define TAGJAVA
find $(1) -type f -name '*.java' -print |\
xargs etags --append --output=$(2) &&
endef
TAGS tags: . $(LIBRARIES) $(ANDROIDSOURCES)
	rm -f TAGS.tmp
	$(foreach d,$^,$(call TAGJAVA,$(d),TAGS.tmp)) true
	cmp -s ./TAGS TAGS.tmp || rm -f ./TAGS && mv TAGS.tmp ./TAGS
.PHONY: TAGS tags


distclean: clean
	rm -f TAGS
.PHONY: distclean
