import os
from glob import glob

from setuptools import find_packages, setup

package_name = 'navigation_service'

setup(
    name=package_name,
    version='0.0.1',
    packages=find_packages(),
    data_files=[
        ('share/ament_index/resource_index/packages', ['resource/' + package_name]),
        ('share/' + package_name, ['package.xml']),
        (os.path.join('share', package_name, 'launch'), glob('launch/*.launch.py')),
    ],
    install_requires=['setuptools'],
    zip_safe=True,
    maintainer='Maryam Yousuf',
    maintainer_email='maryam@aisleon.dev',
    description='navigation_service package for Aisleon retail robot',
    license='Apache-2.0',
    tests_require=['pytest'],
    entry_points={
        'console_scripts': [
            'navigation_node = navigation_service.navigation_node:main',
        ],
    },
)
