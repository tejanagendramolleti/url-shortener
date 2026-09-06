angular.module('urlShortenerApp', [])
.controller('MainController', ['$http', '$window', '$timeout', function($http, $window, $timeout) {
    var vm = this;

    // Server Info
    vm.serverHost = $window.location.host;
    vm.apiBase = '/api/urls';

    // State
    vm.loading = false;
    vm.fetching = false;
    vm.links = [];
    vm.searchQuery = '';
    vm.pageSize = 10;
    
    vm.newUrl = {
        originalUrl: '',
        customKey: '',
        description: ''
    };
    
    vm.createdResult = null;

    vm.pagination = {
        number: 0,
        totalPages: 0,
        totalElements: 0,
        first: true,
        last: true
    };

    vm.toast = {
        visible: false,
        message: '',
        isError: false
    };

    // Show Toast Notification
    vm.showToast = function(message, isError) {
        vm.toast.message = message;
        vm.toast.isError = !!isError;
        vm.toast.visible = true;
        
        $timeout(function() {
            vm.toast.visible = false;
        }, 3500);
    };

    // Create Short URL (POST API)
    vm.createShortUrl = function() {
        if (!vm.newUrl.originalUrl) {
            vm.showToast('Please enter a valid URL', true);
            return;
        }

        vm.loading = true;
        vm.createdResult = null;

        var payload = {
            originalUrl: vm.newUrl.originalUrl,
            customKey: vm.newUrl.customKey || null,
            description: vm.newUrl.description || null
        };

        $http.post(vm.apiBase + '/shorten', payload)
            .then(function(response) {
                vm.createdResult = response.data;
                vm.showToast('Short URL created successfully!');
                
                // Reset form
                vm.newUrl = { originalUrl: '', customKey: '', description: '' };
                
                // Refresh list
                vm.loadPaginatedLinks(0);
            })
            .catch(function(error) {
                var msg = 'Failed to shorten URL';
                if (error.data && error.data.message) {
                    msg = error.data.message;
                } else if (error.data && error.data.validationErrors) {
                    msg = Object.values(error.data.validationErrors).join(', ');
                }
                vm.showToast(msg, true);
            })
            .finally(function() {
                vm.loading = false;
            });
    };

    // Load Paginated Links (GET API)
    vm.loadPaginatedLinks = function(page) {
        vm.fetching = true;
        page = page || 0;

        var params = {
            page: page,
            size: vm.pageSize
        };

        if (vm.searchQuery && vm.searchQuery.trim() !== '') {
            params.search = vm.searchQuery.trim();
        }

        $http.get(vm.apiBase, { params: params })
            .then(function(response) {
                var data = response.data;
                vm.links = data.content || [];
                vm.pagination = {
                    number: data.number || 0,
                    totalPages: data.totalPages || 0,
                    totalElements: data.totalElements || 0,
                    first: data.first,
                    last: data.last
                };
            })
            .catch(function(error) {
                console.error('Error fetching links:', error);
                vm.showToast('Failed to load links from server', true);
            })
            .finally(function() {
                vm.fetching = false;
            });
    };

    // Copy URL to Clipboard
    vm.copyToClipboard = function(text) {
        if (!text) return;
        
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(text).then(function() {
                vm.showToast('Link copied to clipboard!');
            }).catch(function() {
                vm.fallbackCopy(text);
            });
        } else {
            vm.fallbackCopy(text);
        }
    };

    vm.fallbackCopy = function(text) {
        var dummy = document.createElement('textarea');
        document.body.appendChild(dummy);
        dummy.value = text;
        dummy.select();
        document.execCommand('copy');
        document.body.removeChild(dummy);
        vm.showToast('Link copied to clipboard!');
    };

    // Format Date
    vm.formatDate = function(dateStr) {
        if (!dateStr) return 'N/A';
        var d = new Date(dateStr);
        return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    // Handlers
    vm.onSearchChange = function() {
        vm.loadPaginatedLinks(0);
    };

    vm.clearSearch = function() {
        vm.searchQuery = '';
        vm.loadPaginatedLinks(0);
    };

    vm.onPageSizeChange = function() {
        vm.loadPaginatedLinks(0);
    };

    vm.goToPage = function(page) {
        if (page >= 0 && page < vm.pagination.totalPages) {
            vm.loadPaginatedLinks(page);
        }
    };

    // Initialize
    vm.loadPaginatedLinks(0);
}]);
