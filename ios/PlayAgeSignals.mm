#import "PlayAgeSignals.h"

@implementation PlayAgeSignals

- (NSDictionary *)getPlayAgeSignals
{
  return nil;
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
  return std::make_shared<facebook::react::NativePlayAgeSignalsSpecJSI>(params);
}

+ (NSString *)moduleName
{
  return @"PlayAgeSignals";
}

@end
