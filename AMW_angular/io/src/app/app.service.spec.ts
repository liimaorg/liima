// Load the implementations that should be tested
import {AppService} from './app.service';

describe('AppService', () => {
  let service: AppService;
  beforeEach(() => {
    service = new AppService();
  });

  it('should return an empty if no navItems have been set', () => {
    expect(service.navItems()).toEqual([])
  });

  it('should return an array of navItems if navItems have been set',
    () => {
      // given
      const items: any[] = [{title: 'aTest', target: '/aTarget'}, {title: 'bTest', target: '/bTarget'}];
      service.set('navItems', items);
      // when then
      expect(service.navItems()).toEqual(items);
    });
});
