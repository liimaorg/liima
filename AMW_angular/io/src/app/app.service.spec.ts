// Load the implementations that should be tested
import { AppService, Keys } from './app.service';

describe('AppService', () => {
  let service: AppService;
  beforeEach(() => {
    service = new AppService();
  });

  it('should return an array of navItems if navItems have been set', () => {
    // given
    const items: any[] = [
      { title: 'aTest', target: '/aTarget' },
      { title: 'bTest', target: '/bTarget' }
    ];
    service.set('navItems', items);
    // when then
    expect(service._state[Keys.NavItems]).toEqual(items);
  });
});
